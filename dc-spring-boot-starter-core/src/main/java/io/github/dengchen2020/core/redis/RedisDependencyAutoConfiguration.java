package io.github.dengchen2020.core.redis;

import io.github.dengchen2020.core.redis.annotation.RedisMessageListener;
import io.github.dengchen2020.core.redis.annotation.TopicType;
import io.github.dengchen2020.core.scheduled.ScheduledConcurrencyAop;
import io.github.dengchen2020.core.utils.IPUtils;
import io.github.dengchen2020.core.utils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import jakarta.annotation.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 引入redis依赖后的自动配置
 * @author xiaochen
 * @since 2024/7/3
 */
@ConditionalOnClass(RedisConnectionFactory.class)
@AutoConfigureAfter(RedisReactiveAutoConfiguration.class)
@Configuration(proxyBeanMethods = false)
public class RedisDependencyAutoConfiguration {

    @ConditionalOnBean(RedisConnectionFactory.class)
    @ConditionalOnMissingBean
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setTaskExecutor(new VirtualThreadTaskExecutor("redis-message-container-"));
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }

    @ConditionalOnBean(StringRedisTemplate.class)
    @Bean
    public ScheduledConcurrencyAop scheduledConcurrencyAop(StringRedisTemplate redisTemplate, Environment environment, ApplicationEventPublisher applicationEventPublisher){
        return new ScheduledConcurrencyAop(redisTemplate, environment, applicationEventPublisher);
    }

    @ConditionalOnBean(ReactiveRedisConnectionFactory.class)
    @ConditionalOnMissingBean
    @Bean
    public RedisMessagePublisher redisMessagePublisher(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer){
        return new RedisMessagePublisher(new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, RedisSerializationContext.byteArray()), genericJackson2JsonRedisSerializer);
    }

    @Configuration(proxyBeanMethods = false)
    static class RedisMessageListenerRegistrar {

        private static final Logger log = LoggerFactory.getLogger(RedisMessageListenerRegistrar.class);

        public RedisMessageListenerRegistrar(RedisMessageListenerContainer redisMessageListenerContainer, @Nullable List<MessageListener> messageListeners, GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer){
            if (messageListeners != null) {
                for (MessageListener messageListener : messageListeners) {
                    Topic topic;
                    Method[] methods = MethodUtils.getMethodsWithAnnotation(messageListener.getClass(), RedisMessageListener.class);
                    if(methods.length == 0) continue;
                    Method method = methods[0];
                    if(methods.length > 1){
                        log.warn("检测到多个方法,只有一个能生效，实际映射到：{}", method.toString());
                    }
                    RedisMessageListener redisMessageListener = method.getAnnotation(RedisMessageListener.class);
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
                            messageListenerAdapter.setSerializer(genericJackson2JsonRedisSerializer);
                        }
                    }
                    redisMessageListenerContainer.addMessageListener(messageListener, topic);
                }
            }
        }

    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer dcLettuceClientConfigurationBuilderCustomizer(RedisProperties redisProperties, Environment environment){
        return clientConfigurationBuilder -> {
            if (StringUtils.hasText(redisProperties.getClientName())) return;
            String applicationName = environment.getProperty("spring.application.name", "spring");
            Integer port = environment.getProperty("server.port", Integer.class);
            clientConfigurationBuilder.clientName(applicationName + "-" + (port == null ? IPUtils.getLocalAddr() : IPUtils.getLocalAddr() + ":" + port));
        };
    }

}
