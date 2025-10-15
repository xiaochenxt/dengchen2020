package io.github.dengchen2020.core.utils.sign;

/**
 * 签名异常基类
 * @author xiaochen
 * @since 2025/3/12
 */
public class HMACException extends RuntimeException {

    public HMACException() {
    }

    public HMACException(String message) {
        super(message);
    }

    public HMACException(String message, Throwable cause) {
        super(message, cause);
    }

    public HMACException(Throwable cause) {
        super(cause);
    }

    public HMACException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
