package io.github.dengchen2020.websocket.handler;

import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.security.Principal;
import java.util.Set;

/**
 * 基于spring封装的websocket的抽象处理器，推荐使用
 *
 * @author xiaochen
 * @since 2024/6/26
 */
@NullMarked
public abstract class AbstractDcWebSocketHandler extends AbstractWebSocketHandler implements DcWebSocketHandler {

    /**
     * 处理来自基础 WebSocket的 文本消息
     *
     * @param session websocket会话
     * @param message 文本消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        if (log.isDebugEnabled()) log.debug("收到文本消息：{}，客户端：{}", message, session);
    }

    /**
     * 处理来自基础 WebSocket的 二进制数据消息
     *
     * @param session websocket会话
     * @param message 二进制数据消息
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        if (log.isDebugEnabled()) log.debug("收到二进制数据消息:{}，客户端：{}", message, session);
    }

    /**
     * 处理来自基础 WebSocket的 pong消息
     *
     * @param session websocket会话
     * @param message pong消息
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) {
        if (log.isDebugEnabled()) log.debug("收到pong消息:{}，客户端：{}", message, session);
    }

    /**
     * 在任一端关闭 WebSocket 连接后调用，或者在发生传输错误后调用。尽管从技术上讲，会话可能仍处于打开状态，但根据基础实现，不鼓励此时发送消息，并且很可能不会成功
     *
     * @param session websocket会话
     * @param status  表示 WebSocket 关闭状态代码和原因。1xxx 范围内的状态代码由协议预定义。或者，可以发送带有原因的状态代码
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        clear(session);
        if (CLOSE_CODE.contains(status.getCode())) {
            if (log.isDebugEnabled()) log.debug("连接关闭，原因是：{}，客户端：{}", status, session);
        } else {
            log.warn("连接关闭，原因是：{}，客户端：{}", status, session);
        }
        var principal = session.getPrincipal();
        if (principal != null && !(principal instanceof AnonymousAuthentication)) offlineEvent(principal);
    }

    /**
     * 连接关闭后清理资源
     * @param session websocket会话
     */
    protected void clear(WebSocketSession session) {

    }

    /**
     * 处理来自基础 WebSocket 消息传输的错误
     *
     * @param session   websocket会话
     * @param exception 异常对象
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        if (log.isDebugEnabled()) log.debug("连接发生异常，原因是：{}，客户端：{}", exception, session);
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<Integer> CLOSE_CODE = Set.of(CloseStatus.NORMAL.getCode(), CloseStatus.GOING_AWAY.getCode(), CloseStatus.NO_CLOSE_FRAME.getCode(), CloseStatus.POLICY_VIOLATION.getCode());

    /**
     * 在 WebSocket 协商成功且 WebSocket 连接打开并可供使用后调用
     *
     * @param session websocket会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Principal principal = getClientInfo(session);
        if (principal == null || principal instanceof AnonymousAuthentication) {
            CloseStatus status = CloseStatus.POLICY_VIOLATION.withReason("获取Token认证信息失败，请重新登录");
            onlineFailEvent(session, status);
            close(session, status);
            return;
        }
        session.getAttributes().put(CONCURRENT_DECORATOR, concurrentDecorator(session));
        online(session, principal);
        onlineSuccessEvent(session);
    }

    /**
     * 包装成可并发发送消息的WebSocketSession，例如：
     * <pre>
     * new ConcurrentWebSocketSessionDecorator(session, 10 * 1000, 128 * 1024)
     * </pre>
     *
     * @param session 原WebSocketSession
     * @return 新WebSocketSession
     */
    protected ConcurrentWebSocketSessionDecorator concurrentDecorator(WebSocketSession session) {
        var bufferLimit = Math.min(128 * 1024, Math.max(session.getTextMessageSizeLimit(), session.getBinaryMessageSizeLimit()) * 2);
        return new ConcurrentWebSocketSessionDecorator(session,1000 * 10,bufferLimit);
    }

    /**
     * 上线
     *
     * @param session websocket会话
     */
    protected void online(WebSocketSession session, Principal principal) {

    }

    /**
     * 上线成功事件
     *
     * @param session websocket会话
     */
    protected void onlineSuccessEvent(WebSocketSession session) {
        if (log.isDebugEnabled()) log.debug("上线成功，客户端：{}，信息：{}", session, getClientInfo(session));
    }

    /**
     * 上线失败事件
     *
     * @param session websocket会话
     * @param status  异常原因状态代码
     */
    protected void onlineFailEvent(WebSocketSession session, CloseStatus status) {
        if (log.isDebugEnabled()) log.debug("上线失败，原因：{}，客户端：{}", status, session);
    }

    /**
     * 下线事件
     *
     * @param principal 用户信息
     */
    protected void offlineEvent(Principal principal) {

    }

}
