package io.github.dengchen2020.ratelimiter.exception;

import io.github.dengchen2020.core.exception.ViewToastException;

import java.util.concurrent.TimeUnit;

/**
 * 被限流异常
 * @author xiaochen
 * @since 2024/7/1
 */
public class RateLimitException extends ViewToastException {
    private final TimeUnit timeUnit;
    public RateLimitException(String message, TimeUnit timeUnit) {
        super(message, ViewToastException.CODE, null, false);
        this.timeUnit = timeUnit;
    }

    /**
     * 限流时间级别，可通过判断如果为秒级返回http429错误来允许客户端安全重试
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }
}
