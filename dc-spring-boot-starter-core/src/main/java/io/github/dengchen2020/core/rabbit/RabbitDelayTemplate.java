package io.github.dengchen2020.core.rabbit;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.connection.PublisherCallbackChannel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

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

    /**
     * 消息无延迟
     */
    private static final long X_DELAY_MIN = 0;
    /**
     * 重试时的最小延迟时间
     */
    private static final long RETRY_DELAY_MIN = 1000;

    public RabbitDelayTemplate(RabbitTemplate rabbitTemplate) {
        this(rabbitTemplate, RabbitConstant.DELAY_EXCHANGE, X_DELAY_MIN);
    }

    public RabbitDelayTemplate(RabbitTemplate rabbitTemplate, String defaultExchange, long defaultDelay) {
        this.rabbitTemplate = rabbitTemplate;
        this.defaultExchange = defaultExchange;
        this.defaultDelay = Math.max(defaultDelay, X_DELAY_MIN);
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
     * @param time 延迟时间，最多49天（4294967295毫秒）
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
     * @param time     延迟时间，最多49天（4294967295毫秒）
     */
    public void send(String exchange, String routingKey, Object obj,@Nullable Duration time) {
        long millis = time == null ? defaultDelay :
                Math.clamp(time.toMillis(), X_DELAY_MIN, MessageProperties.X_DELAY_MAX);
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        //发送延迟消息
        rabbitTemplate.convertAndSend(exchange, routingKey, obj, message -> {
            var messageProperties = message.getMessageProperties();
            //消息持久化
            messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            //设置延迟时间，毫秒。
            if (millis > 0) messageProperties.setHeader(MessageProperties.X_DELAY, millis);
            return message;
        }, correlationData);
    }

    /**
     * 适用于在{@code @}{@link RabbitListener}方法中重新发送延迟消息，不能用于首次发送，首次发送应使用{@link #send(String, String, Object, Duration)}
     *
     * @param message        消息对象
     * @param time     延迟时间，最多49天（4294967295毫秒）
     * @param maxRetryCount  最大重试次数
     * @return 是否执行了重新发送延迟消息，如果返回false则表示已达到重试次数上限，返回true只表示发送了，不关心成功还是失败
     */
    public boolean resend(Message message, Duration time, int maxRetryCount) {
        var messageProperties = message.getMessageProperties();
        messageProperties.incrementRetryCount();
        if (messageProperties.getRetryCount() > maxRetryCount) return false;
        //消息持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        //设置延迟时间，毫秒。
        long millis = Math.clamp(time.toMillis(), RETRY_DELAY_MIN, MessageProperties.X_DELAY_MAX);
        messageProperties.setDelayLong(millis);
        messageProperties.setHeader(MessageProperties.RETRY_COUNT, messageProperties.getRetryCount());
        var correlationId = messageProperties.getHeader(PublisherCallbackChannel.RETURNED_MESSAGE_CORRELATION_KEY);
        CorrelationData correlationData = new CorrelationData(correlationId != null ? correlationId.toString() : UUID.randomUUID().toString());
        //发送延迟消息
        rabbitTemplate.send(messageProperties.getReceivedExchange(), messageProperties.getReceivedRoutingKey(), message, correlationData);
        return true;
    }

    /**
     * 适用于在{@code @}{@link RabbitListener}方法中重新发送延迟消息（阶梯退避重试，最多重试15次，所有重试延迟累加总时长约24小时4分钟），不能用于首次发送，首次发送应使用{@link #send(String, String, Object, Duration)}
     *
     * @param message        消息对象
     * @param maxRetryCount  最大重试次数，最多15次
     * @return 是否执行了重新发送延迟消息，如果返回false则表示已达到重试次数上限，返回true只表示发送了，不关心成功还是失败
     */
    public boolean resend(Message message, int maxRetryCount) {
        var retryCount = message.getMessageProperties().getRetryCount();
        var delay = switch ((int) retryCount) {
            case 0,1 -> Duration.ofSeconds(15);
            case 2 -> Duration.ofSeconds(30);
            case 3 -> Duration.ofMinutes(3);
            case 4 -> Duration.ofMinutes(10);
            case 5 -> Duration.ofMinutes(20);
            case 6,7,8 -> Duration.ofMinutes(30);
            case 9 -> Duration.ofHours(1);
            case 10,11,12 -> Duration.ofHours(3);
            case 13,14 -> Duration.ofHours(6);
            default -> null;
        };
        if (delay != null) return resend(message, delay, Math.clamp(maxRetryCount, 1, 15));
        return false;
    }

}

