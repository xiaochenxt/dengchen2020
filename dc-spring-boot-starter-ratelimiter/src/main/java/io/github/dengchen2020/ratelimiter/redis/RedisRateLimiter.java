package io.github.dengchen2020.ratelimiter.redis;

import io.github.dengchen2020.core.Version;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.output.IntegerListOutput;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * 基于Redis实现的分布式限流实现
 * @author xiaochen
 * @since 2024/8/3
 */
public class RedisRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final boolean versionAbove8_8_0;

    private static final String RATE_LIMIT_PREFIX = "dc:rate_limit:";

    private static final String INCREX = "INCREX";
    private static final byte[] UBOUND = "UBOUND".getBytes(StandardCharsets.UTF_8);
    private static final byte[] EX = "EX".getBytes(StandardCharsets.UTF_8);
    private static final byte[] ENX = "ENX".getBytes(StandardCharsets.UTF_8);

    private static final RedisScript<Long> rateLimitScript = new DefaultRedisScript<>(
            """ 
                    local rateLimitKey = KEYS[1]
                    local rateLimitValue = redis.call("GET", rateLimitKey)
                    if rateLimitValue then
                        local limitNum = tonumber(rateLimitValue);
                        local rateLimitNum = tonumber(ARGV[1])
                        if limitNum > rateLimitNum then
                            return limitNum
                        else
                            return redis.call("INCR", rateLimitKey)
                        end
                    else
                        local rateLimitSecond = tonumber(ARGV[2])
                        redis.call("SET", rateLimitKey, "1", "EX", rateLimitSecond)
                        return 1
                    end
                    """,
            Long.class
    );

    @SuppressWarnings("unchecked")
    private List<Long> increx(String key, long ubound, long exSecond) {
        return redisTemplate.execute((RedisCallback<List<Long>>) connection -> {
            var command = (DefaultStringRedisConnection)connection.commands();
            var delegate = (LettuceConnection)command.getDelegate();
            return (List<Long>) delegate.execute(INCREX, new IntegerListOutput<>(ByteArrayCodec.INSTANCE), key.getBytes(), UBOUND, String.valueOf(ubound).getBytes(), EX, String.valueOf(exSecond).getBytes(), ENX);
        });
    }

    /**
     * 分布式限流实例化
     * @param redisTemplate {@link StringRedisTemplate}
     *
     */
    public RedisRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        var version = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public @NonNull String doInRedis(@NonNull RedisConnection connection) throws DataAccessException {
                return (String)Objects.requireNonNull(connection.serverCommands().info()).get("redis_version");
            }
        });
        this.versionAbove8_8_0 = Version.compareVersion(version, "8.8.0") >= 0;
    }

    /**
     * 是否被限制
     *
     * @param limitKey 限制标识符
     * @param limitNum 限制的次数
     * @param duration 时间窗口
     * @return true：被限制 false：未被限制
     */
    public boolean limit(String limitKey, int limitNum, Duration duration) {
        if (versionAbove8_8_0) return increx(limitKey, limitNum, duration.toSeconds()).get(1) == 0;
        Long count = redisTemplate.execute(rateLimitScript, List.of(RATE_LIMIT_PREFIX + limitKey), String.valueOf(limitNum), String.valueOf(duration.toSeconds()));
        return count > limitNum;
    }
}
