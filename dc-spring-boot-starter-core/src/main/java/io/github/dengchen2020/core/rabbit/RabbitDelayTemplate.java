package io.github.dengchen2020.core.rabbit;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.UUID;

/**
 * rabbitmq发送延迟消息
 * @author xiaochen
 * @since 2026/6/23
 */
@NullMarked
public class RabbitDelayTemplate {

    private final RabbitTemplate rabbitTemplate;
    private final String defaultExchange;
    private final long defaultDelay;

    public RabbitDelayTemplate(RabbitTemplate rabbitTemplate) {
        this(rabbitTemplate, RabbitConstant.DELAY_EXCHANGE, 0);
    }

    public RabbitDelayTemplate(RabbitTemplate rabbitTemplate, String defaultExchange, long defaultDelay) {
        this.rabbitTemplate = rabbitTemplate;
        this.defaultExchange = defaultExchange;
        this.defaultDelay = Math.max(defaultDelay, 0);
    }

    /**
     * 发送延迟消息，使用延迟交换机
     * <p>使用默认延迟时间，如果默认延迟时间为0，则实际没有延迟</p>
     *
     * @param routingKey 路由键
     * @param obj        消息对象
     */
    public void send(String routingKey, Object obj) {
        send(defaultExchange, routingKey, obj, null);
    }

    /**
     * 发送延迟消息，使用延迟交换机
     *
     * @param routingKey 路由键
     * @param obj        消息对象
     * @param time 延迟时间
     */
    public void send(String routingKey,Object obj, Duration time) {
        send(defaultExchange, routingKey, obj, time);
    }

    /**
     * 发送延迟消息，使用延迟交换机
     *
     * @param exchange   延迟交换机
     * @param routingKey 路由键
     * @param obj        消息对象
     * @param time     延迟时间
     */
    public void send(String exchange, String routingKey, Object obj,@Nullable Duration time) {
        Assert.notNull(exchange, "交换机不能为null");
        Assert.notNull(routingKey, "路由键不能为null");
        Assert.notNull(obj, "消息对象不能为null");
        long millis = time == null ? defaultDelay :
                Math.clamp(time.toMillis(), defaultDelay, MessageProperties.X_DELAY_MAX);
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        //发送延迟消息
        rabbitTemplate.convertAndSend(exchange, routingKey, obj, message -> {
            //消息持久化
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            //设置延迟时间，毫秒。
            message.getMessageProperties().setHeader(MessageProperties.X_DELAY, millis);
            return message;
        }, correlationData);
    }

}

