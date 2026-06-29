package io.github.dengchen2020.core.redis;

import io.github.dengchen2020.core.redis.annotation.RedisListener;
import io.github.dengchen2020.core.scheduled.ScheduledPreventConcurrencyAop;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
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
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * 引入redis依赖后的自动配置
 * @author xiaochen
 * @since 2024/7/3
 */
@AutoConfigureAfter(DataRedisAutoConfiguration.class)
@ConditionalOnBean(ReactiveRedisConnectionFactory.class)
@Configuration(proxyBeanMethods = false)
public final class RedisDependencyAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
        final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        var taskExecutor = new SimpleAsyncTaskExecutor("redisMessageListenerContainer-");
        taskExecutor.setVirtualThreads(true);
        container.setTaskExecutor(taskExecutor);
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }

    @ConditionalOnMissingBean
    @Bean
    RedisMessagePublisher redisMessagePublisher(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory, GenericJacksonJsonRedisSerializer genericJacksonJsonRedisSerializer){
        return new RedisMessagePublisher(new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, RedisSerializationContext.byteArray()), genericJacksonJsonRedisSerializer);
    }

    @Bean
    static LazyInitializationExcludeFilter redisMessageListenerRegistrarLazyInitializationExcludeFilter() {
        return (beanName, beanDefinition, beanType) -> {
            for (Method m : beanType.getDeclaredMethods()) {
                var redisListener = m.getAnnotation(RedisListener.class);
                if (redisListener == null) continue;
                if (m.getParameterCount() == 0 || !Modifier.isPublic(m.getModifiers())) continue;
                return true;
            }
            return false;
        };
    }

    @Bean
    static BeanPostProcessor redisMessageListenerRegistrar(ObjectProvider<RedisMessageListenerContainer> redisMessageListenerContainer, ObjectProvider<GenericJacksonJsonRedisSerializer> genericJacksonJsonRedisSerializer){
        return new RedisMessageListenerRegistrar(redisMessageListenerContainer, genericJacksonJsonRedisSerializer);
    }

    static final class RedisMessageListenerRegistrar implements BeanPostProcessor, Ordered {

        private static final Logger log = LoggerFactory.getLogger(RedisMessageListenerRegistrar.class);
        private final ObjectProvider<RedisMessageListenerContainer> redisMessageListenerContainer;
        private final ObjectProvider<GenericJacksonJsonRedisSerializer> genericJacksonJsonRedisSerializer;

        RedisMessageListenerRegistrar(ObjectProvider<RedisMessageListenerContainer> redisMessageListenerContainer, ObjectProvider<GenericJacksonJsonRedisSerializer> genericJacksonJsonRedisSerializer){
            this.redisMessageListenerContainer = redisMessageListenerContainer;
            this.genericJacksonJsonRedisSerializer = genericJacksonJsonRedisSerializer;
        }

        @Override
        public @NonNull Object postProcessAfterInitialization(@NonNull Object bean,@NonNull String beanName) throws BeansException {
            List<Method> methods = null;
            for (var m : AopUtils.getTargetClass(bean).getDeclaredMethods()) {
                var redisListener = m.getAnnotation(RedisListener.class);
                if (redisListener == null) continue;
                if (m.getParameterCount() == 0 || !Modifier.isPublic(m.getModifiers())) continue;
                if (methods == null) methods = new ArrayList<>(1);
                methods.add(m);
            }
            if (CollectionUtils.isEmpty(methods)) return bean;
            for (Method method : methods) {
                var redisListener = method.getAnnotation(RedisListener.class);
                var topicValue = redisListener.value();
                if (topicValue.isBlank()) throw new IllegalArgumentException("@RedisListener value is required");
                Topic topic = containsPatternGlobs(topicValue) ? PatternTopic.of(topicValue) : ChannelTopic.of(topicValue);
                var methodName = method.getName();
                var messageListenerAdapter = new MessageListenerAdapter(bean, methodName);
                messageListenerAdapter.afterPropertiesSet();
                Class<?> argClass = method.getParameterTypes()[0];
                if(argClass == byte[].class){
                    messageListenerAdapter.setSerializer(RedisSerializer.byteArray());
                }else if(argClass == String.class){
                    messageListenerAdapter.setSerializer(RedisSerializer.string());
                }else {
                    messageListenerAdapter.setSerializer(genericJacksonJsonRedisSerializer.getIfAvailable());
                }
                redisMessageListenerContainer.getIfAvailable().addMessageListener(messageListenerAdapter, topic);
                if (log.isDebugEnabled()) log.debug("redis消息订阅：{}，频道：{}", method, topic);
            }
            return bean;
        }

        /**
         * @return {@literal true} if {@code destinationName} contains an unescaped glob meta character.
         */
        private static boolean containsPatternGlobs(String destinationName) {

            for (int i = 0; i < destinationName.length(); i++) {

                char c = destinationName.charAt(i);
                if (c == '\\') {
                    i++;
                    continue;
                }
                if (c == '?' || c == '*' || c == '[') {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    @Bean
    ScheduledPreventConcurrencyAop scheduledPreventConcurrencyAop(StringRedisTemplate redisTemplate, Environment environment, ApplicationEventPublisher applicationEventPublisher){
        return new ScheduledPreventConcurrencyAop(redisTemplate, environment, applicationEventPublisher);
    }

}
