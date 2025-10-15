package io.github.dengchen2020.core.utils.encrypt;

/**
 * RSA加密异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class RSAEncryptException extends RSAException {

    public RSAEncryptException() {
    }

    public RSAEncryptException(String message) {
        super(message);
    }

    public RSAEncryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public RSAEncryptException(Throwable cause) {
        super(cause);
    }

    public RSAEncryptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
