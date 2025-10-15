package io.github.dengchen2020.websocket.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.SessionLimitExceededException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Principal;

public interface DcWebSocketHandler {

    Logger log = LoggerFactory.getLogger(DcWebSocketHandler.class);

    PingMessage pingMessage = new PingMessage();

    /**
     * 初始化会话配置
     *
     * @param session websocket会话
     */
    void initSessionConfig(WebSocketSession session);

    /**
     * 上线
     *
     * @param session websocket会话
     */
    void online(WebSocketSession session);

    /**
     * 获取客户端信息
     *
     * @param session websocket会话
     * @return 客户端信息
     */
    Principal getClientInfo(WebSocketSession session);

    /**
     * 关闭连接
     *
     * @param session websocket会话
     * @param closeStatus 关闭原因
     */
    default void close(WebSocketSession session, CloseStatus closeStatus) {
        try {
            session.close(closeStatus);
        } catch (IOException e) {
            log.error("关闭连接失败，异常信息：{}", e.toString());
        }
    }

    /**
     * 关闭连接
     *
     * @param session websocket会话
     */
    default void close(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            log.error("关闭连接失败，异常信息：{}", e.toString());
        }
    }

    /**
     * 发送ping消息
     *
     * @param session websocket会话
     */
    default void sendPing(WebSocketSession session) {
        try {
            session.sendMessage(pingMessage);
        } catch (SessionLimitExceededException e) {
            close(session, e.getStatus());
        } catch (Exception e) {
            if (session.isOpen()) log.error("发送ping消息失败，异常信息：{}", e.toString());
        }
    }

    /**
     * 向用户发送文本消息
     *
     * @param session websocket会话
     * @param message 文本消息
     */
    default void send(WebSocketSession session, String message) {
        send(session, message, true);
    }

    /**
     * 向用户发送文本消息
     *
     * @param session websocket会话
     * @param message 文本消息
     * @param isLast 这是否是一个完整消息的最后一部分，true：已发送完一个完整的消息，false：消息只发了一部分，还没发完
     */
    default void send(WebSocketSession session, String message, boolean isLast) {
        try {
            session.sendMessage(new TextMessage(message, isLast));
        } catch (SessionLimitExceededException e) {
            close(session, e.getStatus());
        } catch (Exception e) {
            if (session.isOpen()) log.error("发送文本消息失败：{}，异常信息：{}", message, e.toString());
        }
    }

    /**
     * 向用户发送二进制数据消息
     *
     * @param session    websocket会话
     * @param byteBuffer 二进制数据
     */
    default void send(WebSocketSession session, ByteBuffer byteBuffer) {
        send(session, byteBuffer, true);
    }

    /**
     * 向用户发送二进制数据消息
     *
     * @param session    websocket会话
     * @param byteBuffer 二进制数据
     * @param isLast 这是否是一个完整消息的最后一部分，true：已发送完一个完整的消息，false：消息只发了一部分，还没发完
     */
    default void send(WebSocketSession session, ByteBuffer byteBuffer, boolean isLast) {
        try {
            session.sendMessage(new BinaryMessage(byteBuffer, isLast));
        } catch (SessionLimitExceededException e) {
            close(session, e.getStatus());
        } catch (Exception e) {
            if (session.isOpen()) log.error("发送二进制数据消息失败：{}，异常信息：{}", byteBuffer, e.toString());
        }
    }

}
