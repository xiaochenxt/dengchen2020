package io.github.dengchen2020.core.utils.encrypt;

/**
 * AES异常基类
 * @author xiaochen
 * @since 2025/3/13
 */
public class AESException extends RuntimeException {

    public AESException() {
    }

    public AESException(String message) {
        super(message);
    }

    public AESException(String message, Throwable cause) {
        super(message, cause);
    }

    public AESException(Throwable cause) {
        super(cause);
    }

    public AESException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
