package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.security.principal.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.UUID;

/**
 * 有状态Token认证实现
 * <p>对比无状态</p>
 * <p>优势：对Token的控制力强，可以踢人下线，实时封禁等更多功能</p>
 * <p>劣势：依赖Redis，增加了运维成本</p>
 * @author xiaochen
 * @since 2024/4/24
 */
public class TokenServiceImpl implements TokenService, StateToken {

    private static final Logger log = LoggerFactory.getLogger(TokenServiceImpl.class);

    private static final String SEPARATOR = "-";

    private final AuthenticationConvert authenticationConvert;

    private final StringRedisTemplate stringRedisTemplate;

    private final long expireSeconds;

    private final int maxOnlineNum;

    private final boolean autorenewal;

    private final String autorenewalSeconds;

    public TokenServiceImpl(StringRedisTemplate stringRedisTemplate, AuthenticationConvert authenticationConvert, long expireSeconds, int maxOnlineNum, String device, boolean autorenewal, int autorenewalSeconds) {
        this.authenticationConvert = authenticationConvert;
        this.expireSeconds = expireSeconds;
        this.maxOnlineNum = Math.max(maxOnlineNum, 1);
        this.stringRedisTemplate = stringRedisTemplate;
        if (autorenewal) {
            if (autorenewalSeconds <= 0) throw  new IllegalArgumentException("autorenewalSeconds must be greater than 0");
            if (autorenewalSeconds > expireSeconds / 2) throw new IllegalArgumentException("autorenewalSeconds must be less than expireSeconds / 2");
        }
        this.autorenewal = autorenewal;
        this.autorenewalSeconds = String.valueOf(autorenewalSeconds);
        if (StringUtils.hasText(device)) {
            this.tokenPrefix = TOKEN_COMMON_PREFIX + device + ":";
        }else {
            this.tokenPrefix = TOKEN_COMMON_PREFIX;
        }
        this.tokenInfoPrefix = TOKEN_INFO_KEY;
        initScript();
    }

    public TokenServiceImpl(StringRedisTemplate stringRedisTemplate, AuthenticationConvert authenticationConvert, long expireSeconds, String device, boolean autorenewal, int renewalSeconds) {
        this(stringRedisTemplate, authenticationConvert, expireSeconds,1, device, autorenewal, renewalSeconds);
    }

    public TokenServiceImpl(StringRedisTemplate stringRedisTemplate, AuthenticationConvert authenticationConvert, long expireSeconds, String device) {
        this(stringRedisTemplate, authenticationConvert, expireSeconds,1, device, false, 0);
    }

    public TokenServiceImpl(StringRedisTemplate stringRedisTemplate, AuthenticationConvert authenticationConvert, long expireSeconds, int maxOnlineNum) {
        this(stringRedisTemplate, authenticationConvert, expireSeconds, maxOnlineNum, "", false, 0);
    }

    public TokenServiceImpl(StringRedisTemplate stringRedisTemplate, AuthenticationConvert authenticationConvert, long expireSeconds, int maxOnlineNum, String device) {
        this(stringRedisTemplate, authenticationConvert, expireSeconds, maxOnlineNum, device, false, 0);
    }

    private final String tokenPrefix;

    private final String tokenInfoPrefix;

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public String getTokenInfoPrefix() {
        return tokenInfoPrefix;
    }

    private void initScript() {
        // token存储在list，payload关联用户ID存储，检查在线数量是否超过限制，如果超过限制移除第一个token
        onlineScript = new DefaultRedisScript<>(
                """
                local token = ARGV[1]
                local userId = ARGV[2]
                local payload = ARGV[3]
                local maxOnlineNum = tonumber(ARGV[4])
                local expireTimeInSec = tonumber(ARGV[5])
                local userListKey = "%s" .. userId
                redis.call("RPUSH", userListKey, token)
                redis.call("EXPIRE", userListKey, expireTimeInSec)
                redis.call("SET", "%s" .. userId, payload, "EX", expireTimeInSec);
                local onlineNum = redis.call("LLEN", userListKey)
                if onlineNum > maxOnlineNum then
                    redis.call("LPOP", userListKey)
                end
                """.formatted(tokenPrefix, tokenInfoPrefix), Void.class);

        // 清空userId对应的tokenList，删除关联的payload
        offlineScript = new DefaultRedisScript<>(
                """ 
                local userId = ARGV[1]
                redis.call("UNLINK", "%s" .. userId)
                redis.call("UNLINK", "%s" .. userId)
                """.formatted(tokenPrefix, tokenInfoPrefix),
                Void.class
        );

        // 根据用户id获取token列表，遍历token列表判断token是否存在，存在返回关联的payload，否则返回null
        readTokenScript = new DefaultRedisScript<>(
                """ 
                local token = ARGV[1]
                local userId = ARGV[2]
                local tokens = redis.call("LRANGE", "%s" .. userId, 0, -1)
                for _, t in ipairs(tokens) do
                    if t == token then
                        return redis.call("GET", "%s" .. userId)
                    end
                end
                return nil
                """.formatted(tokenPrefix, tokenInfoPrefix),
                String.class
        );

        // 根据用户id获取token列表，遍历token列表判断token是否存在，不存在返回null，存在则查询token有效期，如果快过期则重新设置有效期，最后返回关联的payload
        readTokenAutorenewalScript = new DefaultRedisScript<>(
                """ 
                local token = ARGV[1]
                local userId = ARGV[2]
                local userListKey = "%s" .. userId
                local tokenInfoKey = "%s" .. userId
                local tokenFound = 0
                local tokens = redis.call("LRANGE", userListKey, 0, -1)
                for _, t in ipairs(tokens) do
                    if t == token then
                        tokenFound = 1
                        break
                    end
                end
                if tokenFound == 1 then
                    local ttl = redis.call('TTL', userListKey)
                    local refreshThreshold = tonumber(ARGV[3])
                    if ttl ~= -1 and ttl < refreshThreshold then
                        local newTtl = tonumber(ARGV[4])
                        redis.call('EXPIRE', userListKey, newTtl)
                        redis.call('EXPIRE', tokenInfoKey, newTtl)
                    end
                    return redis.call("GET", tokenInfoKey)
                end
                return nil
                """.formatted(tokenPrefix, tokenInfoPrefix),
                String.class
        );
    }

    // 上线脚本
    RedisScript<Void> onlineScript;

    // 下线脚本
    RedisScript<Void> offlineScript;

    // 读取Token信息脚本
    RedisScript<String> readTokenScript;

    // 读取Token信息并在快过期时续期脚本
    RedisScript<String> readTokenAutorenewalScript;


    @Override
    public TokenInfo createToken(Authentication authentication) {
        return createToken(authentication, maxOnlineNum, expireSeconds);
    }

    /**
     * 创建token
     *
     * @param authentication 认证信息对象
     * @param maxOnlineNum 最大在线数量
     * @return Token信息
     */
    public TokenInfo createToken(Authentication authentication, int maxOnlineNum) {return createToken(authentication, maxOnlineNum, expireSeconds);}

    /**
     * 创建token
     *
     * @param authentication 认证信息对象
     * @param expireSeconds 过期秒数
     * @return Token信息
     */
    public TokenInfo createToken(Authentication authentication, long expireSeconds) {return createToken(authentication, maxOnlineNum, expireSeconds);}

    /**
     * 创建token
     *
     * @param authentication 认证信息对象
     * @param maxOnlineNum 最大在线数量
     * @param expireSeconds 过期秒数
     * @return Token信息
     */
    public TokenInfo createToken(Authentication authentication, int maxOnlineNum, long expireSeconds) {
        String token = authentication.getName() + SEPARATOR + UUID.randomUUID().toString().replace("-", "");
        String payload = authenticationConvert.serialize(authentication);
        long expiresIn = System.currentTimeMillis() + expireSeconds * 1000;
        stringRedisTemplate.execute(onlineScript, Collections.emptyList(), token, authentication.getName(), payload, String.valueOf(maxOnlineNum), String.valueOf(expireSeconds), String.valueOf(System.currentTimeMillis()));
        return new TokenInfo(token, expiresIn);
    }

    /**
     * 刷新认证信息
     * @param authentication 认证信息对象
     */
    public void refreshAuthentication(Authentication authentication) {
        try {
            stringRedisTemplate.opsForValue().setIfPresent(tokenInfoPrefix + authentication.getName(), authenticationConvert.serialize(authentication));
        } catch (IllegalArgumentException _) {
            if (log.isDebugEnabled()) log.debug("token已失效，认证信息无法刷新");
        }
    }

    @Override
    public Authentication readToken(String token) {
        String tokenInfo;
        if (autorenewal) {
            tokenInfo = stringRedisTemplate.execute(readTokenAutorenewalScript, Collections.emptyList(), token, getName(token), autorenewalSeconds, String.valueOf(expireSeconds));
        }else {
            tokenInfo = stringRedisTemplate.execute(readTokenScript, Collections.emptyList(), token, getName(token));
        }
        if (!StringUtils.hasText(tokenInfo)) return null;
        return authenticationConvert.deserialize(tokenInfo);
    }

    @Override
    public void removeToken(String token) {
        String tokenSetKey = tokenPrefix + getName(token);
        stringRedisTemplate.opsForList().remove(tokenSetKey, 1, token);
    }

    @Override
    public void offline(String userId) {
        stringRedisTemplate.execute(offlineScript, Collections.emptyList(), userId);
    }

    public String getName(String token) {
        if (token == null) return "";
        return token.split(SEPARATOR)[0];
    }

    public long onlineNum(String token) {
        Long num = stringRedisTemplate.opsForList().size(tokenPrefix + getName(token));
        return num == null ? 0 : num;
    }

}

