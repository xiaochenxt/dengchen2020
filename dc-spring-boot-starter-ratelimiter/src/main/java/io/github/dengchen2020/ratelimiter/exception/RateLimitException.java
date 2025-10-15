package io.github.dengchen2020.ratelimiter.exception;

import io.github.dengchen2020.core.exception.call.ViewToastException;

/**
 * 被限流异常
 * @author xiaochen
 * @since 2024/7/1
 */
public class RateLimitException extends ViewToastException {
    public RateLimitException(String message) {
        super(message, ViewToastException.CODE, null, false);
    }
}
