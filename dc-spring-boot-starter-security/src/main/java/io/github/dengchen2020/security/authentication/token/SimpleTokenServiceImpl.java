package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.security.principal.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

import static io.github.dengchen2020.security.authentication.token.TokenConstant.SEPARATOR;

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
                local payload = ARGV[2]
                local expireTimeInSec = tonumber(ARGV[3])
                local userTokenKey = KEYS[1]
                local userInfoKey = KEYS[2]
                redis.call("SET", userTokenKey, token, "EX", expireTimeInSec)
                redis.call("SET", userInfoKey, payload, "EX", expireTimeInSec);
                """, Void.class);
        offlineScript = new DefaultRedisScript<>(
                """
                local userTokenKey = KEYS[1]
                local userInfoKey = KEYS[2]
                redis.call("UNLINK", userTokenKey)
                redis.call("UNLINK", userInfoKey)
                """,
                Void.class
        );
        readTokenScript = new DefaultRedisScript<>(
                """ 
                local token = ARGV[1]
                local userTokenKey = KEYS[1]
                local userInfoKey = KEYS[2]
                local storedToken = redis.call("GET", userTokenKey)
                if storedToken and storedToken == token then
                    return redis.call("GET", userInfoKey)
                end
                return nil
                """,
                String.class
        );
        readTokenAutorenewalScript = new DefaultRedisScript<>(
                """ 
                local token = ARGV[1]
                local userTokenKey = KEYS[1]
                local userInfoKey = KEYS[2]
                local storedToken = redis.call("GET", userTokenKey)
                if storedToken and storedToken == token then
                    local ttl = redis.call('TTL', userTokenKey)
                    local refreshThreshold = tonumber(ARGV[2])
                    if ttl ~= -1 and ttl < refreshThreshold then
                        local newTtl = tonumber(ARGV[3])
                        redis.call('EXPIRE', userTokenKey, newTtl)
                        redis.call('EXPIRE', userInfoKey, newTtl)
                        ttl = newTtl
                    end
                    return redis.call("GET", userInfoKey)
                end
                return nil
                """,
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
        String token = authentication.getName() + SEPARATOR + UUID.randomUUID().toString().replace("-", "");
        String payload = authenticationConvert.serialize(authentication);
        long expiresIn = System.currentTimeMillis() + expireSeconds * 1000;
        var slot = slot(authentication.getName());
        stringRedisTemplate.execute(onlineScript, List.of(tokenPrefix + slot, tokenInfoPrefix + slot), token, payload, String.valueOf(expireSeconds));
        return new TokenInfo(token, expiresIn);
    }

    /**
     * 刷新认证信息
     * @param authentication 认证信息对象
     */
    public void refreshAuthentication(Authentication authentication) {
        try {
            stringRedisTemplate.opsForValue().setIfPresent(tokenInfoPrefix + slot(authentication.getName()), authenticationConvert.serialize(authentication));
        } catch (IllegalArgumentException _) {
            if (log.isDebugEnabled()) log.debug("token已失效，认证信息无法刷新");
        }
    }

    @Override
    public Authentication readToken(String token) {
        String tokenInfo;
        var slot = slot(getName(token));
        var keys = List.of(tokenPrefix + slot, tokenInfoPrefix + slot);
        if (autorenewal) {
            tokenInfo = stringRedisTemplate.execute(readTokenAutorenewalScript, keys, token, autorenewalSeconds, String.valueOf(expireSeconds));
        }else {
            tokenInfo = stringRedisTemplate.execute(readTokenScript, keys, token);
        }
        if (!StringUtils.hasText(tokenInfo)) return null;
        return authenticationConvert.deserialize(tokenInfo);
    }

    @Override
    public void removeToken(String token) {
        String userTokenKey = tokenPrefix + slot(getName(token));
        stringRedisTemplate.unlink(userTokenKey);
    }

    @Override
    public void offline(String userId) {
        var slot = slot(userId);
        stringRedisTemplate.execute(offlineScript, List.of(tokenPrefix + slot, tokenInfoPrefix + slot));
    }

    public String getName(String token) {
        if (token == null) return "";
        return token.split(SEPARATOR)[0];
    }

    private String slot(String userId) {
        return "{" + userId + "}";
    }

}

