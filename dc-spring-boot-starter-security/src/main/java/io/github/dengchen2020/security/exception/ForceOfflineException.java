package io.github.dengchen2020.security.exception;

/**
 * 访问时被强制下线，前端需跳转登录
 * @author xiaochen
 * @since 2024/7/24
 */
public class ForceOfflineException extends SessionTimeOutException {

    public static final String DEFAULT_MESSAGE = "您已被强制下线";

    public ForceOfflineException(String message) {
        super(message);
    }

    public ForceOfflineException() {
        super(DEFAULT_MESSAGE);
    }

}
