package io.github.dengchen2020.websocket.handler.cluster;


import io.github.dengchen2020.core.redis.RedisMessagePublisher;
import org.jspecify.annotations.NullMarked;
import org.springframework.web.socket.CloseStatus;

import java.nio.ByteBuffer;

/**
 * websocket服务器集群时使用该工具发送消息
 *
 * @author xiaochen
 * @since 2023/7/18
 */
@NullMarked
public class WebSocketHelper {

    public WebSocketHelper(RedisMessagePublisher redisMessagePublisher) {this("default", redisMessagePublisher);}

    public WebSocketHelper(String topic, RedisMessagePublisher redisMessagePublisher) {
        this.topic = defaultTopicPrefix + topic;
        this.redisMessagePublisher = redisMessagePublisher;
    }

    static final String defaultTopicPrefix = "dc:websocket:";

    private final String topic;

    public String topic() {
        return topic;
    }

    private final RedisMessagePublisher redisMessagePublisher;

    public WebSocketHelper getInstance(String topic) {
        if (this.topic.equals(defaultTopicPrefix + topic)) return this;
        return new WebSocketHelper(topic, redisMessagePublisher);
    }

    /**
     *
     * 发送websocket消息
     *
     * @param param 集群websocket服务器通知参数
     */
    private void send(WebSocketSendParam param) {
        redisMessagePublisher.publish(topic, param);
    }

    /**
     * 集群环境-关闭用户连接
     *
     * @param userId 用户id
     * @param closeStatus 关闭连接原因
     */
    public void close(String userId, CloseStatus closeStatus) {
        send(new WebSocketSendParam(userId, closeStatus));
    }

    /**
     * 集群环境-关闭用户连接
     *
     * @param userId 用户id
     * @param closeStatus 关闭连接原因
     */
    public void close(String[] userId, CloseStatus closeStatus) {
        send(new WebSocketSendParam(userId, closeStatus));
    }

    /**
     * 集群环境-关闭租户下所有用户的连接
     *
     * @param tenantId 租户id
     * @param closeStatus 关闭连接原因
     */
    public void close(Long tenantId, CloseStatus closeStatus) {
        send(new WebSocketSendParam(tenantId, closeStatus));
    }

    /**
     * 集群环境-向用户发送文本消息
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    public void send(String userId, String message) {
        send(new WebSocketSendParam(userId, message));
    }

    /**
     * 集群环境-向用户发送文本消息
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    public void send(String[] userId, String message) {
        send(new WebSocketSendParam(userId, message));
    }

    /**
     * 集群环境-向用户发送文本消息
     *
     * @param tenantId 租户id
     * @param message  文本消息
     */
    public void send(Long tenantId, String message) {
        send(new WebSocketSendParam(tenantId, message));
    }

    /**
     * 集群环境-向所有用户发送文本消息
     *
     * @param message  文本消息
     */
    public void sendToAll(String message) {
        send(new WebSocketSendParam(message));
    }

    /**
     * 集群环境-向用户发送二进制消息
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    public void send(String userId, ByteBuffer message) {
        send(new WebSocketSendParam(userId, message));
    }

    /**
     * 集群环境-向用户发送二进制消息
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    public void send(String[] userId, ByteBuffer message) {
        send(new WebSocketSendParam(userId, message));
    }

    /**
     * 集群环境-向用户发送二进制消息
     *
     * @param tenantId 租户id
     * @param message  二进制消息
     */
    public void send(Long tenantId, ByteBuffer message) {
        send(new WebSocketSendParam(tenantId, message));
    }

    /**
     * 集群环境-向所有用户发送二进制消息
     *
     * @param message  二进制消息
     */
    public void sendToAll(ByteBuffer message) {
        send(new WebSocketSendParam(message));
    }

}
