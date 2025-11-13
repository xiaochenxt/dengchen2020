package io.github.dengchen2020.core.rabbit.exception;

/**
 * RabbitMQ消息消费异常（推荐使用）<br>
 * 用于记录消费异常时更详细的错误日志
 * @author xiaochen
 * @since 2024/8/22
 */
public class RabbitConsumeException extends RuntimeException {

    public RabbitConsumeException(Throwable cause) {
        super(cause);
    }

    public RabbitConsumeException(String message, Throwable cause) {
        super(message, cause);
    }

}
