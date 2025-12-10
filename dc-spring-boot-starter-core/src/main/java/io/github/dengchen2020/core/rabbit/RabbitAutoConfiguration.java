package io.github.dengchen2020.core.rabbit;

import io.github.dengchen2020.core.utils.StrUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.task.VirtualThreadTaskExecutor;

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
    MessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
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
                Object body = messageConverter.fromMessage(message);
                if (ack) {
                    Long receivedDelay = message.getMessageProperties().getReceivedDelayLong();
                    String handleTime = receivedDelay == null || receivedDelay <= 30000 ? "，" : "，预计处理时间：" + LocalDateTime.now().plusSeconds(receivedDelay / 1000);
                    if (log.isDebugEnabled()) log.debug("消息发送成功 --> 消息id：{}{}，消息：{}，交换机：{}，队列：{}，路由键：{}", correlationData.getId(), handleTime, body, returned.getExchange(), message.getMessageProperties().getConsumerQueue(), returned.getRoutingKey());
                } else {
                    log.error("消息发送失败 --> 消息id：{}，消息：{}，交换机：{}，队列：{}，路由键：{}，回应码：{}，回应信息：{}，异常：{}", correlationData.getId(), body, returned.getExchange(), message.getMessageProperties().getConsumerQueue(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText(), cause);
                }
            }
        };
    }

    @ConditionalOnMissingBean
    @Bean
    RabbitTemplate.ReturnsCallback returnsCallback(MessageConverter messageConverter) {
        return returned -> {
            //排除延时任务：因为发送方确实没有投递到队列上，只是在交换器上暂存，等过期时间到了 才会发往队列
            if (returned.getMessage().getMessageProperties().getReceivedDelayLong() != null) {
                if (returned.getReplyCode() == 312 && "NO_ROUTE".equals(returned.getReplyText())) {
                    return;
                }
            }
            Object body = messageConverter.fromMessage(returned.getMessage());
            log.error("消息发送失败回调 --> 消息id：{}，消息：{}，交换机：{}，队列：{}，路由键：{}，回应码：{}，回应信息：{}", returned.getMessage().getMessageProperties().getHeader(RabbitConstant.RETURNED_MESSAGE_CORRELATION_KEY), body, returned.getExchange(), returned.getMessage().getMessageProperties().getConsumerQueue(), returned.getRoutingKey(), returned.getReplyCode(), returned.getReplyText());
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
            log.error(StrUtils.format("消息处理失败回调 --> 消息id：{}，消息：{}，交换机：{}，队列：{}，路由键：{}，异常信息：", messageProperties.getHeader(RabbitConstant.RETURNED_MESSAGE_CORRELATION_KEY), messageConverter.fromMessage(message), messageProperties.getReceivedExchange(), messageProperties.getConsumerQueue(), messageProperties.getReceivedRoutingKey()), cause);
        };
    }

    /**
     * 死信队列
     */
    @Bean
    Queue deadLetterQueue() {
        Map<String, Object> args = new HashMap<>();
        return new Queue(RabbitConstant.DEAD_LETTER_QUEUE, true, false, false, args);
    }

    /**
     * 死信交换机
     */
    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitConstant.DEAD_LETTER_EXCHANGE);
    }

    /**
     * 避免spring.main.lazy-initialization=true时的警告，详见：<a href="https://github.com/spring-projects/spring-amqp/issues/3251">When spring.main.lazy-initialization=true is enabled, warning logs appear during startup</a>
     */
    @ConditionalOnProperty(value = "spring.main.lazy-initialization", havingValue = "true")
    @Bean
    LazyInitializationExcludeFilter dcRabbitLazyInitializationExcludeFilter() {
        return LazyInitializationExcludeFilter.forBeanTypes(CachingConnectionFactory.class);
    }

}
