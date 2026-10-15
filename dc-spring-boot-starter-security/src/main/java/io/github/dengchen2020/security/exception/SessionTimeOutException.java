package io.github.dengchen2020.security.exception;

import io.github.dengchen2020.core.exception.BaseException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * 登录会话超时异常：前端需跳转登录
 *
 * @author xiaochen
 * @since 2022/07/09 14:11
 */
public class SessionTimeOutException extends BaseException {

    public static final int CODE = 401;

    public static final String DEFAULT_MESSAGE = "会话超时，请重新登录";

    public SessionTimeOutException(String message) {
        super(message, CODE, null, false);
    }

    public SessionTimeOutException() {
        super(DEFAULT_MESSAGE, CODE, null, false);
    }

    @Override
    public @NonNull HttpStatusCode getStatusCode() {
        return HttpStatus.UNAUTHORIZED;
    }
}
