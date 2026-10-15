package io.github.dengchen2020.ratelimiter.exception;

import io.github.dengchen2020.core.exception.ViewToastException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.concurrent.TimeUnit;

/**
 * 被限流异常
 * @author xiaochen
 * @since 2024/7/1
 */
public class RateLimitException extends ViewToastException {

    private HttpHeaders headers;

    private final TimeUnit timeUnit;
    private final int time;
    private final int count;

    public RateLimitException(String message, TimeUnit timeUnit, int time, int count) {
        super(message, ViewToastException.CODE, null, false);
        this.timeUnit = timeUnit;
        this.time = time;
        this.count = count;
    }

    /**
     * 限流时间级别，可通过判断如果为秒级返回http429错误来允许客户端安全重试
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * 限流时间窗口
     */
    public int getTime() {
        return time;
    }

    /**
     * 在限流时间窗口中允许的次数
     */
    public int getCount() {
        return count;
    }

    @Override
    public @NonNull HttpStatusCode getStatusCode() {
        return HttpStatus.TOO_MANY_REQUESTS;
    }

    @Override
    public @NonNull HttpHeaders getHeaders() {
        if (headers == null) {
            headers = new HttpHeaders();
            headers.set(HttpHeaders.RETRY_AFTER, String.valueOf(getTimeUnit().toSeconds(getTime())));
        }
        return headers;
    }
}
