package io.github.dengchen2020.id.exception;

/**
 * id生成异常
 * @author xiaochen
 * @since 2024/7/1
 */
public class IdGeneratorException extends RuntimeException {

    public IdGeneratorException() {
    }

    public IdGeneratorException(String message) {
        super(message);
    }

    public IdGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdGeneratorException(Throwable cause) {
        super(cause);
    }

    public IdGeneratorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public IdGeneratorException(String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
    }

}

