package io.github.dengchen2020.core.rabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.PublisherCallbackChannel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Rabbit自动配置
 *
 * @author xiaochen
 * @since 2024/8/21
 */
@PropertySource("classpath:application-rabbit.properties")
@ConditionalOnClass(MessageConverter.class)
@Configuration(proxyBeanMethods = false)
public final class RabbitAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RabbitAutoConfiguration.class);

    @ConditionalOnMissingBean
    @Bean
    MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @ConditionalOnMissingBean
    @Bean
    RabbitTemplate.ConfirmCallback confirmCallback(MessageConverter messageConverter) {
        return (correlationData, ack, cause) -> {
            if (correlationData == null) {
                if (ack) {
                    if (log.isDebugEnabled()) log.debug("消息发送成功");
                } else {
                    log.error("消息发送失败，原因：{}", cause);
                }
                return;
            }
            ReturnedMessage returned = correlationData.getReturned();
            if (returned == null) {
                if (ack) {
                    if (log.isDebugEnabled()) log.debug("消息发送成功，消息id：{}", correlationData.getId());
                } else {
                    log.error("消息发送失败，消息id：{}，原因：{}", correlationData.getId(), cause);
                }
            } else {
                Message message = returned.getMessage();
                var messageProperties = message.getMessageProperties();
                Object body = messageConverter.fromMessage(message);
                if (ack) {
                    if (log.isDebugEnabled()) {
                        Long receivedDelay = messageProperties.getReceivedDelayLong();
                        String handleTime = receivedDelay == null || receivedDelay <= 5000 ? "" : "，预计处理时间：" + LocalDateTime.now().plusSeconds(receivedDelay / 1000);
                        log.debug("消息发送成功 --> 消息id：{}{}，消息：{}，交换机：{}，队列：{}，路由键：{}", correlationData.getId(), handleTime, body, returned.getExchange(), messageProperties.getConsumerQueue(), returned.getRoutingKey());
                    }
                } else {
                    log.error("消息发送失败 --> 消息id：{}，消息：{}，交换机：{}，队列：{}，路由键：{}，回应码：{}，回应信息：{}，异常：{}", correlationData.getId(), body, returned.getExchange(), messageProperties.getConsumerQueue(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText(), cause);
                }
            }
        };
    }

    @ConditionalOnMissingBean
    @Bean
    RabbitTemplate.ReturnsCallback returnsCallback(MessageConverter messageConverter) {
        return returned -> {
            var messageProperties = returned.getMessage().getMessageProperties();
            //排除延时任务：因为发送方确实没有投递到队列上，只是在交换器上暂存，等过期时间到了 才会发往队列
            if (messageProperties.getReceivedDelayLong() != null) {
                if (returned.getReplyCode() == 312 && "NO_ROUTE".equals(returned.getReplyText())) {
                    return;
                }
            }
            Object body = messageConverter.fromMessage(returned.getMessage());
            log.error("消息路由失败回调 --> 消息id：{}，消息：{}，交换机：{}，队列：{}，路由键：{}，回应码：{}，回应信息：{}", messageProperties.getHeader(PublisherCallbackChannel.RETURNED_MESSAGE_CORRELATION_KEY), body, returned.getExchange(), messageProperties.getConsumerQueue(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText());
        };
    }

    @Bean
    RabbitTemplateCustomizer dcRabbitTemplateCustomizer(RabbitTemplate.ConfirmCallback confirmCallback, RabbitTemplate.ReturnsCallback returnsCallback) {
        return template -> {
            template.setTaskExecutor(new VirtualThreadTaskExecutor("rabbitmq-"));
            template.setConfirmCallback(confirmCallback);
            template.setReturnsCallback(returnsCallback);
        };
    }

    @ConditionalOnMissingBean
    @Bean
    MessageRecoverer messageRecoverer(MessageConverter messageConverter) {
        return (message, cause) -> {
            MessageProperties messageProperties = message.getMessageProperties();
            log.error("消息处理失败回调 --> 消息id：{}，消息：{}，交换机：{}，队列：{}，路由键：{}", messageProperties.getHeader(PublisherCallbackChannel.RETURNED_MESSAGE_CORRELATION_KEY), messageConverter.fromMessage(message), messageProperties.getReceivedExchange(), messageProperties.getConsumerQueue(), messageProperties.getReceivedRoutingKey());
            throw new AmqpRejectAndDontRequeueException("Retry Policy Exhausted", cause);
        };
    }

    /**
     * 死信交换机
     */
    @ConditionalOnMissingBean(name = "deadLetterExchange")
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitConstant.DEAD_LETTER_EXCHANGE);
    }

    /**
     * 死信队列
     */
    @ConditionalOnMissingBean(name = "deadLetterQueue")
    @Bean
    Queue deadLetterQueue() {
        Map<String, Object> args = new HashMap<>();
        return new Queue(RabbitConstant.DEAD_LETTER_QUEUE, true, false, false, args);
    }

    /**
     * 死信队列绑定死信交换机
     */
    @ConditionalOnMissingBean(name = "deadLetterBinding")
    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(RabbitConstant.DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * 避免spring.main.lazy-initialization=true时的警告，详见：<a href="https://github.com/spring-projects/spring-amqp/issues/3251">When spring.main.lazy-initialization=true is enabled, warning logs appear during startup</a>
     */
    @ConditionalOnProperty(value = "spring.main.lazy-initialization", havingValue = "true")
    @Bean
    static LazyInitializationExcludeFilter dcRabbitLazyInitializationExcludeFilter() {
        return (beanName, beanDefinition, beanType) -> {
            if (CachingConnectionFactory.class.isAssignableFrom(beanType)) return true;
            if (beanType.isAnnotationPresent(RabbitListener.class)) return true;
            for (Method method : beanType.getDeclaredMethods()) {
                if (method.isAnnotationPresent(RabbitListener.class)) return true;
            }
            return false;
        };
    }

    CustomExchange customExchange(String name, String exchangeType) {
        Map<String, Object> args = CollectionUtils.newHashMap(1);
        args.put(RabbitConstant.X_DELAYED_TYPE, exchangeType);
        return new CustomExchange(name, RabbitConstant.X_DELAYED_MESSAGE, true, false, args);
    }

    /**
     * 声明一个延迟交换机，Direct模式
     * <p>
     * 通过 x-delayed-message 声明的交换机，它的消息在发布之后不会立即进入队列，先将消息保存至 Mnesia，<br/>
     * 等待消息过期通过 x-delayed-type 类型标记的交换机投递至目标队列，整个消息的投递过程也就完成了。
     * <p>
     * 注：延迟最大极限在：0 ~ 2^32 即 4294967296毫秒，49天左右。如果不在这个范围（0 ~ 2^32），延迟将会失效，消息立即进入队列消费
     */
    @ConditionalOnMissingBean(name = "delayExchange")
    @Bean
    CustomExchange delayExchange() {
        return customExchange(RabbitConstant.DELAY_EXCHANGE, ExchangeTypes.DIRECT);
    }

    @ConditionalOnMissingBean
    @Bean
    RabbitDelayTemplate rabbitDelayTemplate(RabbitTemplate rabbitTemplate) {
        return new RabbitDelayTemplate(rabbitTemplate);
    }

}
