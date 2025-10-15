package io.github.dengchen2020.core.utils.encrypt;

/**
 * RSA生成密钥对异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class RSAGenerateKeyException extends RSAException {

    public RSAGenerateKeyException() {
    }

    public RSAGenerateKeyException(String message) {
        super(message);
    }

    public RSAGenerateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public RSAGenerateKeyException(Throwable cause) {
        super(cause);
    }

    public RSAGenerateKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
