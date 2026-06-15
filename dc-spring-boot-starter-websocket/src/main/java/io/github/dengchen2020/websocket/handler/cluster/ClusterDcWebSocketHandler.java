package io.github.dengchen2020.websocket.handler.cluster;

import io.github.dengchen2020.core.redis.RedisMessagePublisher;
import io.github.dengchen2020.websocket.annotation.WebSocketMapping;
import io.github.dengchen2020.websocket.handler.SingletonDcWebSocketHandler;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.web.socket.CloseStatus;

import java.nio.ByteBuffer;

/**
 * 服务器集群的websocket消息处理器 </br>
 * 依赖redis，需引入
 * <pre>{@code
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-data-redis</artifactId>
 * </dependency>}</pre>
 *
 * @author xiaochen
 * @since 2024/6/26
 */
@NullMarked
public abstract class ClusterDcWebSocketHandler extends SingletonDcWebSocketHandler implements InitializingBean {

    private final String topic;

    protected ClusterDcWebSocketHandler() {
        this.topic = getClass().getAnnotation(WebSocketMapping.class).value();
    }

    private WebSocketTemplate webSocketTemplate;
    private RedisMessageListenerContainer redisMessageListenerContainer;
    private GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer;

    @Autowired
    public void setRedisMessagePublisher(RedisMessagePublisher redisMessagePublisher) {
        this.webSocketTemplate = new WebSocketTemplate(topic, redisMessagePublisher);
    }

    @Autowired
    public void setRedisMessageListenerContainer(RedisMessageListenerContainer redisMessageListenerContainer) {
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    @Autowired
    public void setGenericJackson2JsonRedisSerializer(GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer) {
        this.genericJackson2JsonRedisSerializer = genericJackson2JsonRedisSerializer;
    }

    @Override
    public void afterPropertiesSet() {
        if (redisMessageListenerContainer == null) throw new IllegalArgumentException("redisMessageListenerContainer is null");
        if (webSocketTemplate == null) throw new IllegalArgumentException("webSocketHelper is null");
        if (genericJackson2JsonRedisSerializer == null) throw new IllegalArgumentException("genericJackson2JsonRedisSerializer is null");
        MessageListenerAdapter messageListenerAdapter = new ClusterWebSocketMsgListener(this);
        messageListenerAdapter.setSerializer(genericJackson2JsonRedisSerializer);
        messageListenerAdapter.afterPropertiesSet();
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter, ChannelTopic.of(webSocketTemplate.topic()));
    }

    /**
     * 集群环境-关闭用户连接
     *
     * @param userId 用户id
     * @param closeStatus 关闭连接原因
     */
    @Override
    public void close(String userId, CloseStatus closeStatus) {
        webSocketTemplate.close(userId, closeStatus);
    }

    /**
     * 集群环境-关闭用户连接
     *
     * @param userId 用户id
     * @param closeStatus 关闭连接原因
     */
    @Override
    public void close(String[] userId, CloseStatus closeStatus) {
        webSocketTemplate.close(userId, closeStatus);
    }

    /**
     * 集群环境-关闭租户下所有用户的连接
     *
     * @param tenantId 租户id
     * @param closeStatus 关闭连接原因
     */
    @Override
    public void close(Long tenantId, CloseStatus closeStatus) {
        webSocketTemplate.close(tenantId, closeStatus);
    }

    /**
     * 集群环境-向用户发送文本消息
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    @Override
    public void send(String userId, String message) {
        webSocketTemplate.send(userId, message);
    }

    /**
     * 集群环境-向用户发送文本消息
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    @Override
    public void send(String[] userId, String message) {
        webSocketTemplate.send(userId, message);
    }

    /**
     * 集群环境-向用户发送文本消息
     *
     * @param tenantId 租户id
     * @param message  文本消息
     */
    @Override
    public void send(Long tenantId, String message) {
        webSocketTemplate.send(tenantId, message);
    }

    /**
     * 集群环境-向所有用户发送文本消息
     *
     * @param message  文本消息
     */
    @Override
    public void sendToAll(String message) {
        webSocketTemplate.sendToAll(message);
    }

    /**
     * 集群环境-向用户发送二进制消息
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    @Override
    public void send(String userId, ByteBuffer message) {
        webSocketTemplate.send(userId, message);
    }

    /**
     * 集群环境-向用户发送二进制消息
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    @Override
    public void send(String[] userId, ByteBuffer message) {
        webSocketTemplate.send(userId, message);
    }

    /**
     * 集群环境-向用户发送二进制消息
     *
     * @param tenantId 租户id
     * @param message  二进制消息
     */
    @Override
    public void send(Long tenantId, ByteBuffer message) {
        webSocketTemplate.send(tenantId, message);
    }

    /**
     * 集群环境-向所有用户发送二进制消息
     *
     * @param message  二进制消息
     */
    @Override
    public void sendToAll(ByteBuffer message) {
        webSocketTemplate.sendToAll(message);
    }

    /**
     * 关闭连接，详见：{@link SingletonDcWebSocketHandler#close(String, CloseStatus)}
     * @param userId 用户id
     * @param closeStatus 关闭原因
     */
    void closeNoPublish(String userId, CloseStatus closeStatus) {
        super.close(userId, closeStatus);
    }

    /**
     * 关闭连接，详见：{@link SingletonDcWebSocketHandler#close(String[], CloseStatus)}
     * @param userId 用户id
     * @param closeStatus 关闭原因
     */
    void closeNoPublish(String[] userId, CloseStatus closeStatus) {
        super.close(userId, closeStatus);
    }

    /**
     * 关闭租户下所有用户的连接，详见：{@link SingletonDcWebSocketHandler#close(Long, CloseStatus)}
     * @param tenantId 租户id
     * @param closeStatus 关闭原因
     */
    void closeNoPublish(Long tenantId, CloseStatus closeStatus) {
        super.close(tenantId, closeStatus);
    }

    /**
     * 向用户发送文本消息，详见：{@link SingletonDcWebSocketHandler#send(String, String)}
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    void sendNoPublish(String userId, String message) {
        super.send(userId, message);
    }

    /**
     * 向用户发送文本消息，详见：{@link SingletonDcWebSocketHandler#send(String, String)}
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    void sendNoPublish(String[] userId, String message) {
        super.send(userId, message);
    }

    /**
     * 向用户发送文本消息，详见：{@link SingletonDcWebSocketHandler#send(Long, String)}
     *
     * @param tenantId 租户id
     * @param message  文本消息
     */
    void sendNoPublish(Long tenantId, String message) {
        super.send(tenantId, message);
    }

    /**
     * 向所有用户发送文本消息，详见：{@link SingletonDcWebSocketHandler#sendToAll(String)}
     *
     * @param message  文本消息
     */
    void sendToAllNoPublish(String message) {
        super.sendToAll(message);
    }

    /**
     * 向用户发送二进制消息，详见：{@link SingletonDcWebSocketHandler#send(String, ByteBuffer)}
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    void sendNoPublish(String userId, ByteBuffer message) {
        super.send(userId, message);
    }

    /**
     * 向用户发送二进制消息，详见：{@link SingletonDcWebSocketHandler#send(String, ByteBuffer)}
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    void sendNoPublish(String[] userId, ByteBuffer message) {
        super.send(userId, message);
    }

    /**
     * 向用户发送二进制消息，详见：{@link SingletonDcWebSocketHandler#send(Long, ByteBuffer)}
     *
     * @param tenantId 租户id
     * @param message  二进制消息
     */
    void sendNoPublish(Long tenantId, ByteBuffer message) {
        super.send(tenantId, message);
    }

    /**
     * 向所有用户发送二进制消息，详见：{@link SingletonDcWebSocketHandler#sendToAll(ByteBuffer)}
     *
     * @param message  二进制消息
     */
    void sendToAllNoPublish(ByteBuffer message) {
        super.sendToAll(message);
    }

}
