package io.github.dengchen2020.id.exception;

/**
 * id生成异常
 * @author xiaochen
 * @since 2024/7/1
 */
public class IdGeneratorException extends RuntimeException {

    public IdGeneratorException(String message) {
        super(message);
    }

    public IdGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdGeneratorException(String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
    }

}

