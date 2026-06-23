package io.github.dengchen2020.core.rabbit;

/**
 * Rabbit常量
 * @author xiaochen
 * @since 2024/8/22
 */
public final class RabbitConstant {

    private RabbitConstant() {}

    /**
     * 延迟交换机类型
     */
    public static final String X_DELAYED_TYPE = "x-delayed-type";

    /**
     * 延迟消息
     */
    public static final String X_DELAYED_MESSAGE = "x-delayed-message";

    /**
     * 死信交换机，用于将消费失败的消息路由到死信队列
     */
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";

    /**
     * 死信路由键，用于将消费失败的消息路由到死信队列
     */
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    /**
     * 死信交换机
     */
    public static final String DEAD_LETTER_EXCHANGE = "dc.dead.letter.direct";

    /**
     * 死信队列
     */
    public static final String DEAD_LETTER_QUEUE = "dc.dead.letter.queue";

    /**
     * 死信路由键
     */
    public static final String DEAD_LETTER_ROUTING_KEY = "dc.dead.letter";

    /**
     * 延迟交换机
     */
    public static final String DELAY_EXCHANGE = "dc.delay.direct";

}
