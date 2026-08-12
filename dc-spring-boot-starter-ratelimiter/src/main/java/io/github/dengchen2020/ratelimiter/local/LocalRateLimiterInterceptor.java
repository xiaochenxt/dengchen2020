package io.github.dengchen2020.ratelimiter.local;

import io.github.dengchen2020.ratelimiter.AbstractRateLimiterInterceptor;
import io.github.dengchen2020.ratelimiter.annotation.RateLimit;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.TimeUnit;

/**
 * 单机限流拦截器
 *
 * @author xiaochen
 * @since 2024/8/3
 */
@NullMarked
final class LocalRateLimiterInterceptor extends AbstractRateLimiterInterceptor {

    private final LocalRateLimiter secondRateLimiter;

    private final LocalRateLimiter minuteRateLimiter;

    public LocalRateLimiterInterceptor(LocalRateLimiter secondRateLimiter, LocalRateLimiter minuteRateLimiter, String errorMsg) {
        super(errorMsg);
        this.secondRateLimiter = secondRateLimiter;
        this.minuteRateLimiter = minuteRateLimiter;
    }

    @Override
    protected boolean limit(RateLimit rateLimit, String limitKey) {
        LocalRateLimiter rateLimiter;
        if (rateLimit.timeUnit() == TimeUnit.MINUTES) {
            rateLimiter = minuteRateLimiter;
        } else {
            rateLimiter = secondRateLimiter;
        }
        return rateLimiter.limit(limitKey, rateLimit.value());
    }
}




