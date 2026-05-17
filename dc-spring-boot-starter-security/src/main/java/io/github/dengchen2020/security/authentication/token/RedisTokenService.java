package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.security.principal.Authentication;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis实现有状态Token认证（支持每用户最大在线数量控制）
 * <p>对比无状态</p>
 * <p>优势：对Token的控制力强，可以踢人下线，实时封禁等更多功能</p>
 * <p>劣势：依赖Redis，增加了运维成本</p>
 * @author xiaochen
 * @since 2024/4/24
 */
public class RedisTokenService extends AbstractStateTokenService {

    private final int maxOnlineNum;

    public RedisTokenService(long expireSeconds, int maxOnlineNum, String device, boolean autorenewal, long autorenewalSeconds, String tokenName) {
        super(expireSeconds, autorenewal, autorenewalSeconds, tokenName);
        this.maxOnlineNum = Math.max(maxOnlineNum, 1);
        this.tokenPrefix = StringUtils.hasText(device)
                ? TOKEN_COMMON_PREFIX + device + ":"
                : TOKEN_COMMON_PREFIX;
    }

    @Override
    public TokenInfo createToken(Authentication authentication) {
        return createToken(authentication, maxOnlineNum, expireSeconds);
    }

    public TokenInfo createToken(Authentication authentication, int maxOnlineNum) {
        return createToken(authentication, maxOnlineNum, expireSeconds);
    }

    public TokenInfo createToken(Authentication authentication, int maxOnlineNum, long expireSeconds) {
        String token = generateTokenStr(authentication);
        String payload = authenticationConvert.serialize(authentication);
        long expiresIn = System.currentTimeMillis() + expireSeconds * 1000;
        var userId = authentication.userId();
        stringRedisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public @Nullable <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                var redis = ((StringRedisTemplate)operations);
                var tk = tokenKey(userId);
                redis.opsForList().rightPush(tk, token);
                redis.expire(tk, expireSeconds, TimeUnit.SECONDS);
                redis.opsForValue().set(infoKey(userId), payload, expireSeconds, TimeUnit.SECONDS);
                redis.opsForList().trim(tk, -maxOnlineNum, -1);
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
        List<String> tokens = stringRedisTemplate.opsForList().range(tk, 0, -1);
        if (tokens == null || !tokens.contains(token)) return null;
        if (autorenewal) {
            long ttl = stringRedisTemplate.getExpire(tk, TimeUnit.SECONDS);
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
        }
        String info = stringRedisTemplate.opsForValue().get(ik);
        return StringUtils.hasText(info) ? authenticationConvert.deserialize(info) : null;
    }

    /**
     * 使该token失效
     * @param token
     */
    public void removeToken(String token) {
        var userId = getUserId(token);
        stringRedisTemplate.opsForList().remove(tokenKey(userId), 1, token);
    }

    /**
     * 获取token对应的用户在线的设备数量
     * @param token
     * @return
     */
    public long onlineNum(String token) {
        Long num = stringRedisTemplate.opsForList().size(tokenKey(getUserId(token)));
        return num == null ? 0 : num;
    }

}
