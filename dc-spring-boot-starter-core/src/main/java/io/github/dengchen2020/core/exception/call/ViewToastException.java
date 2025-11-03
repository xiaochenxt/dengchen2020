package io.github.dengchen2020.core.exception.call;

import io.github.dengchen2020.core.exception.BaseException;
import org.slf4j.helpers.MessageFormatter;

/**
 * 前端弹框提示异常：前端无法预处理的异常
 *
 * @since 2020/11/26 10:45
 */
public class ViewToastException extends BaseException {

    public static final int CODE = 10444;

    public ViewToastException(String message) {
        super(message, CODE);
    }

    public ViewToastException(String message, Object... args) {
        super(MessageFormatter.arrayFormat(message, args).getMessage(), CODE);
    }

    public ViewToastException(Throwable cause, String message) {
        super(message, CODE, cause);
    }

    public ViewToastException(Throwable cause, String message, Object... args) {
        super(MessageFormatter.arrayFormat(message, args).getMessage(), CODE, cause);
    }

    public ViewToastException(String message, int code, Throwable cause, boolean writableStackTrace) {
        super(message, code, cause, writableStackTrace);
    }

}
