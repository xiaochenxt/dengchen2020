package io.github.dengchen2020.security.exception;

import io.github.dengchen2020.core.exception.call.ViewToastException;

/**
 * 无权限异常
 * @author xiaochen
 * @since 2024/7/22
 */
public class NoPermissionException extends ViewToastException {

    public static final int CODE = 403;

    public static final String DEFAULT_MESSAGE = "您没有权限操作该功能";

    public NoPermissionException(String message) {
        super(message, CODE);
    }

    public NoPermissionException() {
        super(DEFAULT_MESSAGE, CODE);
    }

}
