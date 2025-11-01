package io.github.dengchen2020.websocket.handler.cluster;

import io.github.dengchen2020.websocket.handler.SingletonDcWebSocketHandler;
import org.jspecify.annotations.NullMarked;
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
public class ClusterDcWebSocketHandler extends SingletonDcWebSocketHandler {

    private final WebSocketHelper webSocketHelper;

    public WebSocketHelper webSocketHelper() {
        return webSocketHelper;
    }

    public ClusterDcWebSocketHandler(WebSocketHelper webSocketHelper) {
        this.webSocketHelper = webSocketHelper;
    }

    /**
     * 集群环境-关闭用户连接
     *
     * @param userId 用户id
     * @param closeStatus 关闭连接原因
     */
    @Override
    public void close(String userId, CloseStatus closeStatus) {
        webSocketHelper.close(userId, closeStatus);
    }

    /**
     * 集群环境-关闭用户连接
     *
     * @param userId 用户id
     * @param closeStatus 关闭连接原因
     */
    @Override
    public void close(String[] userId, CloseStatus closeStatus) {
        webSocketHelper.close(userId, closeStatus);
    }

    /**
     * 集群环境-关闭租户下所有用户的连接
     *
     * @param tenantId 租户id
     * @param closeStatus 关闭连接原因
     */
    @Override
    public void close(Long tenantId, CloseStatus closeStatus) {
        webSocketHelper.close(tenantId, closeStatus);
    }

    /**
     * 集群环境-向用户发送文本消息
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    @Override
    public void send(String userId, String message) {
        webSocketHelper.send(userId, message);
    }

    /**
     * 集群环境-向用户发送文本消息
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    @Override
    public void send(String[] userId, String message) {
        webSocketHelper.send(userId, message);
    }

    /**
     * 集群环境-向用户发送文本消息
     *
     * @param tenantId 租户id
     * @param message  文本消息
     */
    @Override
    public void send(Long tenantId, String message) {
        webSocketHelper.send(tenantId, message);
    }

    /**
     * 集群环境-向所有用户发送文本消息
     *
     * @param message  文本消息
     */
    @Override
    public void sendToAll(String message) {
        webSocketHelper.sendToAll(message);
    }

    /**
     * 集群环境-向用户发送二进制消息
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    @Override
    public void send(String userId, ByteBuffer message) {
        webSocketHelper.send(userId, message);
    }

    /**
     * 集群环境-向用户发送二进制消息
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    @Override
    public void send(String[] userId, ByteBuffer message) {
        super.send(userId, message);
    }

    /**
     * 集群环境-向用户发送二进制消息
     *
     * @param tenantId 租户id
     * @param message  二进制消息
     */
    @Override
    public void send(Long tenantId, ByteBuffer message) {
        webSocketHelper.send(tenantId, message);
    }

    /**
     * 集群环境-向所有用户发送二进制消息
     *
     * @param message  二进制消息
     */
    @Override
    public void sendToAll(ByteBuffer message) {
        webSocketHelper.sendToAll(message);
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
