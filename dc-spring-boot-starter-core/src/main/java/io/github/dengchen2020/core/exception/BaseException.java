package io.github.dengchen2020.core.exception;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

/**
 * 异常基类
 *
 * @author xiaochen
 * @since 2019/10/17 11:56
 */
public abstract class BaseException extends RuntimeException implements ErrorResponse {

    private final int code;
    private final ProblemDetail body = ProblemDetail.forStatusAndDetail(getStatusCode(), getMessage());

    public int getCode() {
        return code;
    }

    public BaseException(String message, int code) {
        super(message);
        this.code = code;
    }

    public BaseException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public BaseException(String message, int code, Throwable cause, boolean writableStackTrace) {
        super(message, cause, true, writableStackTrace);
        this.code = code;
    }

    @Override
    public @NonNull HttpStatusCode getStatusCode() {
        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public @NonNull ProblemDetail getBody() {
        return body;
    }
}
