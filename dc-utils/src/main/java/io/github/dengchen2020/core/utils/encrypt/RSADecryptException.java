package io.github.dengchen2020.core.utils.encrypt;

/**
 * RSA解密异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class RSADecryptException extends RSAException {

    public RSADecryptException() {
    }

    public RSADecryptException(String message) {
        super(message);
    }

    public RSADecryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public RSADecryptException(Throwable cause) {
        super(cause);
    }

    public RSADecryptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
