package io.github.dengchen2020.core.rabbit.exception;

import org.slf4j.helpers.MessageFormatter;

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

    public RabbitConsumeException(Throwable cause, String message) {
        super(message, cause);
    }

    public RabbitConsumeException(Throwable cause, String message, Object... args) {
        super(MessageFormatter.arrayFormat(message, args).getMessage(), cause);
    }

}
