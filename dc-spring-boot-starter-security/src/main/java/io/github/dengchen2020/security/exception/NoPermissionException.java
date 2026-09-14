package io.github.dengchen2020.security.exception;

import io.github.dengchen2020.core.exception.ViewToastException;

/**
 * 无权限异常
 * @author xiaochen
 * @since 2024/7/22
 */
public class NoPermissionException extends ViewToastException {

    public static final int CODE = 403;

    public static final String DEFAULT_MESSAGE = "您没有权限操作该功能";

    private final String[] permissions;

    public NoPermissionException(String[] permissions) {
        this(DEFAULT_MESSAGE, permissions);
    }

    public NoPermissionException(String message, String[] permissions) {
        super(message, CODE);
        this.permissions = permissions;
    }

    /**
     * 需要的权限
     */
    public String[] getPermissions() {
        return permissions;
    }
}
