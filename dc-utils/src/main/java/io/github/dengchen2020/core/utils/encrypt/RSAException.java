package io.github.dengchen2020.core.utils.encrypt;

/**
 * RSA异常基类
 * @author xiaochen
 * @since 2025/3/13
 */
public class RSAException extends RuntimeException {

    public RSAException() {
    }

    public RSAException(String message) {
        super(message);
    }

    public RSAException(String message, Throwable cause) {
        super(message, cause);
    }

    public RSAException(Throwable cause) {
        super(cause);
    }

    public RSAException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
