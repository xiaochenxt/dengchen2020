package io.github.dengchen2020.lock.exception;

import io.github.dengchen2020.core.exception.call.ViewToastException;

/**
 * 获锁失败异常
 * @author xiaochen
 * @since 2024/7/1
 */
public class LockException extends ViewToastException {
    public LockException(String message) {
        super(message, ViewToastException.CODE, null, false);
    }
}
