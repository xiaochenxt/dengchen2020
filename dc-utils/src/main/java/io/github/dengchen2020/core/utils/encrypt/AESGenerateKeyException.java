package io.github.dengchen2020.core.utils.encrypt;

/**
 * AES生成密钥异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class AESGenerateKeyException extends AESException {

    public AESGenerateKeyException() {
    }

    public AESGenerateKeyException(String message) {
        super(message);
    }

    public AESGenerateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public AESGenerateKeyException(Throwable cause) {
        super(cause);
    }

    public AESGenerateKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
