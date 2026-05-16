package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.core.utils.StrUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

import static io.github.dengchen2020.security.authentication.token.TokenConstant.SEPARATOR;

/**
 * 基于Redis实现有状态Token认证，每个设备仅能同时在线一个
 * <p>对比无状态</p>
 * <p>优势：对Token的控制力强，可以踢人下线，实时封禁等更多功能</p>
 * <p>劣势：依赖Redis，增加了运维成本</p>
 * @author xiaochen
 * @since 2024/4/24
 */
public class RedisSimpleTokenService extends AbstartStateTokenService {

    public RedisSimpleTokenService(long expireSeconds, String device, boolean autorenewal, long autorenewalSeconds, String tokenName) {
        super(expireSeconds, device, autorenewal, autorenewalSeconds, tokenName);
        this.tokenPrefix = StringUtils.hasText(device)
                ? TOKEN_COMMON_PREFIX + "simp:" + device + ":"
                : TOKEN_COMMON_PREFIX + "simp:";
    }

    @Override
    public TokenInfo createToken(Authentication authentication) {
        return createToken(authentication, expireSeconds);
    }

    public TokenInfo createToken(Authentication authentication, long expireSeconds) {
        String token = authentication.getName() + SEPARATOR + StrUtils.uuidSimplified();
        String payload = authenticationConvert.serialize(authentication);
        long expiresIn = System.currentTimeMillis() + expireSeconds * 1000;
        var userId = authentication.getName();
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
        var userId = getName(token);
        var tk = tokenKey(userId);
        var ik = infoKey(userId);
        String storedToken = stringRedisTemplate.opsForValue().get(tk);
        if (!token.equals(storedToken)) return null;
        if (autorenewal) {
            long ttl = stringRedisTemplate.getExpire(tk, TimeUnit.SECONDS);
            if (ttl > 0 && ttl < autorenewalSeconds) {
                stringRedisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public @Nullable <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                        var redis = ((StringRedisTemplate)operations);
                        redis.expire(tk, expireSeconds, TimeUnit.SECONDS);
                        redis.expire(ik, expireSeconds, TimeUnit.SECONDS);
                        return null;
                    }
                });
            }
        }
        String info = stringRedisTemplate.opsForValue().get(ik);
        return StringUtils.hasText(info) ? authenticationConvert.deserialize(info) : null;
    }

    /**
     * 使该token失效
     * @param token
     */
    public void removeToken(String token) {
        stringRedisTemplate.unlink(tokenKey(getName(token)));
    }

}
