package io.github.dengchen2020.core.exception.call;

import io.github.dengchen2020.core.exception.BaseException;

/**
 * 前端弹框提示异常，该异常需提供国际化支持
 */
public class ToastException extends BaseException {

    public static final int CODE = 400;

    public ToastException(String message) {
        super(message, CODE);
    }

    public ToastException(String message, int code) {
        super(message, code);
    }

    public ToastException(String message, Throwable cause) {
        super(message, CODE, cause);
    }

    public ToastException(String message, int code, Throwable cause) {
        super(message, code, cause);
    }

    public ToastException(String message, int code, Throwable cause, boolean writableStackTrace) {
        super(message, code, cause, writableStackTrace);
    }

}
