package io.github.dengchen2020.ratelimiter.redis;

import io.github.dengchen2020.ratelimiter.RateLimiter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

/**
 * 基于Redis实现的分布式限流实现
 * @author xiaochen
 * @since 2024/8/3
 */
public class RedisRateLimiter implements RateLimiter {

    private final String seconds;

    private final StringRedisTemplate redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "dc:rate_limit:";

    RedisScript<Long> rateLimitScript = new DefaultRedisScript<>(
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

    /**
     * 分布式限流实例化
     * @param duration 限流时间
     * @param redisTemplate {@link StringRedisTemplate}
     *
     */
    public RedisRateLimiter(Duration duration, StringRedisTemplate redisTemplate) {
        this.seconds = String.valueOf(duration.toSeconds());
        this.redisTemplate = redisTemplate;
    }

    /**
     * 是否被限制
     *
     * @param limitKey 限制标识符
     * @param limitNum 限制的次数
     * @return true：被限制 false：未被限制
     */
    @Override
    public boolean limit(String limitKey, int limitNum) {
        Long count = redisTemplate.execute(rateLimitScript, List.of(RATE_LIMIT_PREFIX + limitKey), String.valueOf(limitNum), seconds);
        return count > limitNum;
    }
}
