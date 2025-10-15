package io.github.dengchen2020.core.utils.encrypt;

/**
 * RSA生成公钥异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class RSAGeneratePublicException extends RSAException {

    public RSAGeneratePublicException() {
    }

    public RSAGeneratePublicException(String message) {
        super(message);
    }

    public RSAGeneratePublicException(String message, Throwable cause) {
        super(message, cause);
    }

    public RSAGeneratePublicException(Throwable cause) {
        super(cause);
    }

    public RSAGeneratePublicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
