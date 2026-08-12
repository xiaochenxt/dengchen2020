package io.github.dengchen2020.core.utils.sign;

/**
 * 签名验证异常
 * @author xiaochen
 * @since 2025/3/12
 */
public class HMACVerifyException extends HMACException {

    public HMACVerifyException() {
    }

    public HMACVerifyException(String message) {
        super(message);
    }

    public HMACVerifyException(String message, Throwable cause) {
        super(message, cause);
    }

    public HMACVerifyException(Throwable cause) {
        super(cause);
    }

    public HMACVerifyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
