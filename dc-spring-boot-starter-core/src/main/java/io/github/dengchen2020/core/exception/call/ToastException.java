package io.github.dengchen2020.core.exception.call;

import io.github.dengchen2020.core.exception.BaseException;
import org.slf4j.helpers.MessageFormatter;

/**
 * 前端弹框提示异常，该异常需提供国际化支持
 */
public class ToastException extends BaseException {

    public static final int CODE = 400;

    public ToastException(String message) {
        super(message, CODE);
    }

    public ToastException(String message, Object... args) {
        super(MessageFormatter.arrayFormat(message, args).getMessage(), CODE);
    }

    public ToastException(Throwable cause, String message, Object... args) {
        super(MessageFormatter.arrayFormat(message, args).getMessage(), CODE, cause);
    }

    public ToastException(String message, int code, Throwable cause, boolean writableStackTrace) {
        super(message, code, cause, writableStackTrace);
    }

}
