package io.github.dengchen2020.core.rabbit;

/**
 * Rabbit常量
 * @author xiaochen
 * @since 2024/8/22
 */
public class RabbitConstant {

    /**
     * 死信队列
     */
    public static final String DEAD_LETTER_QUEUE = "dc_queue_deadLetter";

    /**
     * 死信交换机
     */
    public static final String DEAD_LETTER_EXCHANGE = "dc_exchange_deadLetter";

    /**
     * 延迟交换机类型
     */
    public static final String X_DELAYED_TYPE = "x-delayed-type";

    /**
     * 延迟消息
     */
    public static final String X_DELAYED_MESSAGE = "x-delayed-message";

    /**
     * 消息头 correlation_id 的属性名
     */
    public static final String RETURNED_MESSAGE_CORRELATION_KEY = "spring_returned_message_correlation";

}
