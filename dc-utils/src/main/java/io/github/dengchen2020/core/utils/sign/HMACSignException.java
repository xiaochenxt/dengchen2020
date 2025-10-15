package io.github.dengchen2020.core.utils.sign;

/**
 * 签名异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class HMACSignException extends HMACException {

    public HMACSignException() {
    }

    public HMACSignException(String message) {
        super(message);
    }

    public HMACSignException(String message, Throwable cause) {
        super(message, cause);
    }

    public HMACSignException(Throwable cause) {
        super(cause);
    }

    public HMACSignException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
