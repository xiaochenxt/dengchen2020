package io.github.dengchen2020.ratelimiter.redis;

import io.github.dengchen2020.ratelimiter.AbstractRateLimiterInterceptor;
import io.github.dengchen2020.ratelimiter.annotation.RateLimit;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;

/**
 * 分布式限流拦截器
 *
 * @author xiaochen
 * @since 2024/8/3
 */
@NullMarked
final class RedisRateLimiterInterceptor extends AbstractRateLimiterInterceptor {

    private final RedisRateLimiter redisRateLimiter;

    public RedisRateLimiterInterceptor(RedisRateLimiter redisRateLimiter, String errorMsg) {
        super(errorMsg);
        this.redisRateLimiter = redisRateLimiter;
    }

    @Override
    protected boolean limit(RateLimit rateLimit, String limitKey) {
        return redisRateLimiter.limit(limitKey, rateLimit.value(), Duration.of(rateLimit.time(), rateLimit.timeUnit().toChronoUnit()));
    }
}




