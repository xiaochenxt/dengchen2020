package io.github.dengchen2020.core.redis;

import io.github.dengchen2020.core.redis.annotation.RedisMessageListener;
import io.github.dengchen2020.core.redis.annotation.TopicType;
import io.github.dengchen2020.core.scheduled.ScheduledConcurrencyAop;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * 引入redis依赖后的自动配置
 * @author xiaochen
 * @since 2024/7/3
 */
@AutoConfigureAfter({DataRedisAutoConfiguration.class})
@ConditionalOnBean(ReactiveRedisConnectionFactory.class)
@Configuration(proxyBeanMethods = false)
public final class RedisDependencyAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setTaskExecutor(new VirtualThreadTaskExecutor("redis-message-container-"));
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }

    @ConditionalOnMissingBean
    @Bean
    RedisMessagePublisher redisMessagePublisher(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory, GenericJacksonJsonRedisSerializer genericJacksonJsonRedisSerializer){
        return new RedisMessagePublisher(new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, RedisSerializationContext.byteArray()), genericJacksonJsonRedisSerializer);
    }

    @Configuration(proxyBeanMethods = false)
    static final class RedisMessageListenerRegistrar {

        private static final Logger log = LoggerFactory.getLogger(RedisMessageListenerRegistrar.class);

        RedisMessageListenerRegistrar(RedisMessageListenerContainer redisMessageListenerContainer, @Nullable List<MessageListener> messageListeners, GenericJacksonJsonRedisSerializer genericJacksonJsonRedisSerializer){
            if (messageListeners != null) {
                for (MessageListener messageListener : messageListeners) {
                    Method method = null;
                    for (Method m : messageListener.getClass().getDeclaredMethods()) {
                        if (m.getParameterCount() == 0 || !Modifier.isPublic(m.getModifiers())) continue;
                        RedisMessageListener redisMessageListener = m.getAnnotation(RedisMessageListener.class);
                        if (redisMessageListener == null) continue;
                        if (method != null) {
                            throw new IllegalArgumentException(messageListener.getClass() + " 检测到多个使用@RedisMessageListener的方法");
                        }
                        method = m;
                    }
                    if (method == null) continue;
                    RedisMessageListener redisMessageListener = method.getAnnotation(RedisMessageListener.class);
                    Topic topic;
                    if (redisMessageListener.type() == TopicType.channel) {
                        topic = ChannelTopic.of(redisMessageListener.value());
                    } else {
                        topic = PatternTopic.of(redisMessageListener.value());
                    }
                    if(messageListener instanceof MessageListenerAdapter messageListenerAdapter){
                        messageListenerAdapter.setDefaultListenerMethod(method.getName());
                        Class<?> argClass = method.getParameterTypes()[0];
                        if(argClass == byte[].class){
                            messageListenerAdapter.setSerializer(RedisSerializer.byteArray());
                        }else if(argClass == String.class){
                            messageListenerAdapter.setSerializer(RedisSerializer.string());
                        }else {
                            messageListenerAdapter.setSerializer(genericJacksonJsonRedisSerializer);
                        }
                    }
                    redisMessageListenerContainer.addMessageListener(messageListener, topic);
                    if (log.isDebugEnabled()) log.debug("redis消息订阅：{}，频道：{}", method, topic);
                }
            }
        }

    }

    @Bean
    ScheduledConcurrencyAop scheduledConcurrencyAop(StringRedisTemplate redisTemplate, Environment environment, ApplicationEventPublisher applicationEventPublisher){
        return new ScheduledConcurrencyAop(redisTemplate, environment, applicationEventPublisher);
    }

}
