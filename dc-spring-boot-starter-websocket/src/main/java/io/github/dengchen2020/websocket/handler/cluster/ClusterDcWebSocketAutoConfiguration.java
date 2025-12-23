package io.github.dengchen2020.websocket.handler.cluster;

import io.github.dengchen2020.core.redis.RedisDependencyAutoConfiguration;
import io.github.dengchen2020.core.redis.RedisMessagePublisher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;

import java.util.List;

/**
 * websocket集群自动配置
 * @author xiaochen
 * @since 2024/6/27
 */
@AutoConfigureAfter(RedisDependencyAutoConfiguration.class)
@ConditionalOnBean(RedisMessagePublisher.class)
@Configuration(proxyBeanMethods = false)
public final class ClusterDcWebSocketAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    WebSocketHelper webSocketHelper(RedisMessagePublisher redisMessagePublisher) {
        return new WebSocketHelper(redisMessagePublisher);
    }

    @Lazy(false)
    @Configuration(proxyBeanMethods = false)
    static final class ClusterWebSocketHandlerRegistrar {
        ClusterWebSocketHandlerRegistrar(List<ClusterDcWebSocketHandler> clusterDcWebSocketHandler, RedisMessageListenerContainer redisMessageListenerContainer, GenericJacksonJsonRedisSerializer redisSerializer) {
            for (ClusterDcWebSocketHandler dcWebSocketHandler : clusterDcWebSocketHandler) {
                MessageListenerAdapter messageListenerAdapter = new ClusterWebSocketMsgListener(dcWebSocketHandler);
                messageListenerAdapter.setSerializer(redisSerializer);
                messageListenerAdapter.afterPropertiesSet();
                redisMessageListenerContainer.addMessageListener(messageListenerAdapter, ChannelTopic.of(dcWebSocketHandler.webSocketHelper().topic()));
            }
        }
    }

}
