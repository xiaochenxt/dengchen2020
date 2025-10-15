package io.github.dengchen2020.core.utils.encrypt;

/**
 * RSA生成私钥异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class RSAGeneratePrivateException extends RSAException {

    public RSAGeneratePrivateException() {
    }

    public RSAGeneratePrivateException(String message) {
        super(message);
    }

    public RSAGeneratePrivateException(String message, Throwable cause) {
        super(message, cause);
    }

    public RSAGeneratePrivateException(Throwable cause) {
        super(cause);
    }

    public RSAGeneratePrivateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
