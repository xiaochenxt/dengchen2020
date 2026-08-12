package io.github.dengchen2020.core.utils.encrypt;

/**
 * AES加密异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class AESEncryptException extends AESException {

    public AESEncryptException() {
    }

    public AESEncryptException(String message) {
        super(message);
    }

    public AESEncryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public AESEncryptException(Throwable cause) {
        super(cause);
    }

    public AESEncryptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
