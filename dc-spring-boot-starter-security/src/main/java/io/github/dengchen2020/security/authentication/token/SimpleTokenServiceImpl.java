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
 * 有状态Token认证实现，每个设备仅能同时在线一个
 * <p>对比无状态</p>
 * <p>优势：对Token的控制力强，可以踢人下线，实时封禁等更多功能</p>
 * <p>劣势：依赖Redis，增加了运维成本</p>
 * @author xiaochen
 * @since 2024/4/24
 */
public class SimpleTokenServiceImpl implements TokenService, StateToken {

    private static final Logger log = LoggerFactory.getLogger(SimpleTokenServiceImpl.class);

    private final AuthenticationConvert authenticationConvert;

    private final StringRedisTemplate stringRedisTemplate;

    private final long expireSeconds;

    private final boolean autorenewal;

    private final String autorenewalSeconds;

    public SimpleTokenServiceImpl(StringRedisTemplate stringRedisTemplate, AuthenticationConvert authenticationConvert, long expireSeconds, String device, boolean autorenewal, int autorenewalSeconds) {
        this.authenticationConvert = authenticationConvert;
        this.expireSeconds = expireSeconds;
        this.stringRedisTemplate = stringRedisTemplate;
        if (autorenewal) {
            if (autorenewalSeconds <= 0) throw  new IllegalArgumentException("autorenewalSeconds must be greater than 0");
            if (autorenewalSeconds > expireSeconds / 2) throw new IllegalArgumentException("autorenewalSeconds must be less than expireSeconds / 2");
        }
        this.autorenewal = autorenewal;
        this.autorenewalSeconds = String.valueOf(autorenewalSeconds);
        if (StringUtils.hasText(device)) {
            this.tokenPrefix = TOKEN_COMMON_PREFIX + "simp:" + device + ":";
        }else {
            this.tokenPrefix = TOKEN_COMMON_PREFIX + "simp:";
        }
        this.tokenInfoPrefix = TOKEN_INFO_KEY;
        initScript();
    }

    public SimpleTokenServiceImpl(StringRedisTemplate stringRedisTemplate, AuthenticationConvert authenticationConvert, long expireSeconds, String device) {
        this(stringRedisTemplate, authenticationConvert, expireSeconds, device, false, 0);
    }

    public SimpleTokenServiceImpl(StringRedisTemplate stringRedisTemplate, AuthenticationConvert authenticationConvert, long expireSeconds) {
        this(stringRedisTemplate, authenticationConvert, expireSeconds, "", false, 0);
    }

    public SimpleTokenServiceImpl(StringRedisTemplate stringRedisTemplate, AuthenticationConvert authenticationConvert, long expireSeconds, boolean autorenewal, int autorenewalSeconds) {
        this(stringRedisTemplate, authenticationConvert, expireSeconds, "", autorenewal, autorenewalSeconds);
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
        onlineScript = new DefaultRedisScript<>(
                """
                local token = ARGV[1]
                local userId = ARGV[2]
                local payload = ARGV[3]
                local expireTimeInSec = tonumber(ARGV[4])
                local userTokenKey = "%s" .. userId
                redis.call("SET", userTokenKey, token, "EX", expireTimeInSec)
                redis.call("SET", "%s" .. userId, payload, "EX", expireTimeInSec);
                """.formatted(tokenPrefix, tokenInfoPrefix), Void.class);
        offlineScript = new DefaultRedisScript<>(
                """ 
                local userId = ARGV[1]
                local userTokenKey = "%s" .. userId
                redis.call("UNLINK", userTokenKey)
                redis.call("UNLINK", "%s" .. userId)
                """.formatted(tokenPrefix, tokenInfoPrefix),
                Void.class
        );
        readTokenScript = new DefaultRedisScript<>(
                """ 
                local token = ARGV[1]
                local userId = ARGV[2]
                local userTokenKey = "%s" .. userId
                local storedToken = redis.call("GET", userTokenKey)
                if storedToken and storedToken == token then
                    return redis.call("GET", "%s" .. userId)
                end
                return nil
                """.formatted(tokenPrefix, tokenInfoPrefix),
                String.class
        );
        readTokenAutorenewalScript = new DefaultRedisScript<>(
                """ 
                local TOKEN_PREFIX = "%s"
                local TOKEN_INFO_PREFIX = "%s"
                local token = ARGV[1]
                local userId = ARGV[2]
                local userTokenKey = TOKEN_PREFIX .. userId
                local storedToken = redis.call("GET", userTokenKey)
                if storedToken and storedToken == token then
                    local ttl = redis.call('TTL', userTokenKey)
                    local refreshThreshold = tonumber(ARGV[3])
                    if ttl ~= -1 and ttl < refreshThreshold then
                        local newTtl = tonumber(ARGV[4])
                        redis.call('EXPIRE', userTokenKey, newTtl)
                        redis.call('EXPIRE', TOKEN_INFO_PREFIX .. userId, newTtl)
                        ttl = newTtl
                    end
                    return redis.call("GET", TOKEN_INFO_PREFIX .. userId)
                end
                return nil
                """.formatted(tokenPrefix, tokenInfoPrefix),
                String.class
        );
    }

    // 上线脚本：存储token并处理有效期，只保留最新的一个token
    RedisScript<Void> onlineScript;

    // 下线脚本：删除用户的token和关联信息
    RedisScript<Void> offlineScript;

    // 读取Token脚本：检查token有效性并返回用户信息
    RedisScript<String> readTokenScript;

    // 读取Token脚本：检查token有效性并返回用户信息，在token快过期时自动续期
    RedisScript<String> readTokenAutorenewalScript;

    /**
     * 创建token
     *
     * @param authentication 认证信息对象
     * @return Token信息
     */
    public TokenInfo createToken(Authentication authentication) {return createToken(authentication, expireSeconds);}

    /**
     * 创建token
     *
     * @param authentication 认证信息对象
     * @param expireSeconds 过期秒数
     * @return Token信息
     */
    public TokenInfo createToken(Authentication authentication, long expireSeconds) {
        String token = authentication.getName() + "-" + UUID.randomUUID().toString().replace("-", "");
        String payload = authenticationConvert.serialize(authentication);
        long expiresIn = System.currentTimeMillis() + expireSeconds * 1000;
        stringRedisTemplate.execute(onlineScript, Collections.emptyList(), token, authentication.getName(), payload, String.valueOf(expireSeconds));
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
        stringRedisTemplate.opsForZSet().remove(tokenSetKey, token);
    }

    @Override
    public void offline(String userId) {
        stringRedisTemplate.execute(offlineScript, Collections.emptyList(), userId);
    }

    public String getName(String token) {
        if (token == null) return "";
        return token.split("-")[0];
    }

}

