package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.security.exception.SessionTimeOutException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 基于Redis实现有状态Token认证，每个设备仅能同时在线一个
 * <p>对比无状态</p>
 * <p>优势：对Token的控制力强，可以踢人下线，实时封禁等更多功能</p>
 * <p>劣势：依赖Redis，增加了运维成本</p>
 * @author xiaochen
 * @since 2024/4/24
 */
public class RedisSimpleTokenService extends AbstractStateTokenService {

    private final String tokenKeyPrefix;

    public RedisSimpleTokenService(long expireSeconds, String device, boolean autorenewal, long autorenewalSeconds, String tokenName) {
        super(expireSeconds, autorenewal, autorenewalSeconds, tokenName);
        this.tokenKeyPrefix = StringUtils.hasText(device)
                ? TOKEN_COMMON_PREFIX + "simp:" + device + ":"
                : TOKEN_COMMON_PREFIX + "simp:";
    }

    @Override
    protected String tokenKeyPrefix() {
        return tokenKeyPrefix;
    }

    @Override
    public TokenInfo createToken(Authentication authentication) {
        return createToken(authentication, expireSeconds);
    }

    public TokenInfo createToken(Authentication authentication, long expireSeconds) {
        String token = generateTokenStr(authentication);
        String payload = authenticationConvert.serialize(authentication);
        long expiresIn = System.currentTimeMillis() + expireSeconds * 1000;
        var userId = authentication.userId();
        stringRedisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public @Nullable <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                var redis = ((StringRedisTemplate)operations);
                redis.opsForValue().set(tokenKey(userId), token, expireSeconds, TimeUnit.SECONDS);
                redis.opsForValue().set(infoKey(userId), payload, expireSeconds, TimeUnit.SECONDS);
                return null;
            }
        });
        return generateTokenInfo(token, expiresIn);
    }

    @Override
    public @Nullable Authentication readToken(String token) {
        var userId = getUserId(token);
        var tk = tokenKey(userId);
        var ik = infoKey(userId);
        var storedToken = stringRedisTemplate.opsForValue().get(tk);
        if (storedToken == null) return null;
        // 如果当前账号的token存在，但是与前端所给的token不一致
        if (!token.equals(storedToken)) throw new SessionTimeOutException("当前账号已在其他设备登录");
        String info;
        if (autorenewal) {
            var res = stringRedisTemplate.executePipelined(new SessionCallback<>() {
                @Override
                public @Nullable <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                    var redis = ((StringRedisTemplate)operations);
                    redis.getExpire(tk, TimeUnit.SECONDS);
                    redis.opsForValue().get(ik);
                    return null;
                }
            });
            info = (String) res.get(1);
            long ttl = (long) res.getFirst();
            if (ttl > 0 && ttl < autorenewalSeconds) {
                stringRedisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public @Nullable <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                        var redis = ((StringRedisTemplate)operations);
                        redis.expire(tk, expireSeconds, TimeUnit.SECONDS);
                        redis.expire(ik, expireSeconds, TimeUnit.SECONDS);
                        return null;
                    }
                });
            }
        } else {
            info = stringRedisTemplate.opsForValue().get(ik);
        }
        return StringUtils.hasText(info) ? authenticationConvert.deserialize(info) : null;
    }

    /**
     * 使该token失效
     * @param token
     */
    public void removeToken(String token) {
        stringRedisTemplate.unlink(tokenKey(getUserId(token)));
    }

}
