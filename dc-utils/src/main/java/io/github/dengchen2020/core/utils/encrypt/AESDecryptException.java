package io.github.dengchen2020.core.utils.encrypt;

/**
 * AES解密异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class AESDecryptException extends AESException {

    public AESDecryptException() {
    }

    public AESDecryptException(String message) {
        super(message);
    }

    public AESDecryptException(String message, Throwable cause) {
        super(message, cause);
    }

    public AESDecryptException(Throwable cause) {
        super(cause);
    }

    public AESDecryptException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
