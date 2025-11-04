package io.github.dengchen2020.core.exception;

/**
 * 异常基类
 *
 * @author xiaochen
 * @since 2019/10/17 11:56
 */
public abstract class BaseException extends RuntimeException {

    private final int code;

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
        super(message, cause, false, writableStackTrace);
        this.code = code;
    }

}
