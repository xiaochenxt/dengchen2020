package io.github.dengchen2020.websocket.handler;

import jakarta.annotation.Nonnull;
import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.*;
import org.springframework.web.socket.adapter.NativeWebSocketSession;
import org.springframework.web.socket.adapter.jetty.JettyWebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.security.Principal;
import java.time.Duration;
import java.util.Set;

/**
 * 基于spring封装的websocket的抽象处理器，推荐使用
 *
 * @author xiaochen
 * @since 2024/6/26
 */
public abstract class AbstractDcWebSocketHandler extends AbstractWebSocketHandler implements DcWebSocketHandler {

    /**
     * 处理来自基础 WebSocket的 文本消息
     *
     * @param session websocket会话
     * @param message 文本消息
     */
    @Override
    protected void handleTextMessage(@Nonnull WebSocketSession session,@Nonnull TextMessage message) {
        if (log.isDebugEnabled()) log.debug("收到文本消息：{}，客户端：{}", message, session);
    }

    /**
     * 处理来自基础 WebSocket的 二进制数据消息
     *
     * @param session websocket会话
     * @param message 二进制数据消息
     */
    @Override
    protected void handleBinaryMessage(@Nonnull WebSocketSession session,@Nonnull BinaryMessage message) {
        if (log.isDebugEnabled()) log.debug("收到二进制数据消息:{}，客户端：{}", message, session);
    }

    /**
     * 处理来自基础 WebSocket的 pong消息
     *
     * @param session websocket会话
     * @param message pong消息
     */
    @Override
    protected void handlePongMessage(@Nonnull WebSocketSession session,@Nonnull PongMessage message) {
        if (log.isDebugEnabled()) log.debug("收到pong消息:{}，客户端：{}", message, session);
    }

    /**
     * 在任一端关闭 WebSocket 连接后调用，或者在发生传输错误后调用。尽管从技术上讲，会话可能仍处于打开状态，但根据基础实现，不鼓励此时发送消息，并且很可能不会成功
     *
     * @param session websocket会话
     * @param status  表示 WebSocket 关闭状态代码和原因。1xxx 范围内的状态代码由协议预定义。或者，可以发送带有原因的状态代码
     */
    @Override
    public void afterConnectionClosed(@Nonnull WebSocketSession session, CloseStatus status) {
        clear(session);
        if (CLOSE_CODE.contains(status.getCode())) {
            if (log.isDebugEnabled()) log.debug("连接关闭，原因是：{}，客户端：{}", status, session);
        } else {
            log.warn("连接关闭，原因是：{}，客户端：{}", status, session);
        }
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
    public void handleTransportError(@Nonnull WebSocketSession session,@Nonnull Throwable exception) {
        if (log.isDebugEnabled()) log.debug("连接发生异常，原因是：{}，客户端：{}", exception, session);
    }

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final Set<Integer> CLOSE_CODE = Set.of(CloseReason.CloseCodes.NORMAL_CLOSURE.getCode(), CloseReason.CloseCodes.GOING_AWAY.getCode(), CloseReason.CloseCodes.CLOSED_ABNORMALLY.getCode(), CloseReason.CloseCodes.VIOLATED_POLICY.getCode());

    /**
     * 在 WebSocket 协商成功且 WebSocket 连接打开并可供使用后调用
     *
     * @param session websocket会话
     */
    @Override
    public void afterConnectionEstablished(@Nonnull WebSocketSession session) {
        Principal principal = getClientInfo(session);
        if (principal == null) {
            CloseStatus status = CloseStatus.POLICY_VIOLATION.withReason("获取Token认证信息失败，请重新登录");
            onlineFailEvent(session, status);
            close(session, status);
            return;
        }
        initSessionConfig(session);
        online(wrap(session));
        onlineSuccessEvent(session);
    }

    /**
     * 包装WebSocketSession，例如：
     * <pre>
     * new ConcurrentWebSocketSessionDecorator(session, 10 * 1000, 32 * 1024)
     * </pre>
     *
     * @param session 原WebSocketSession
     * @return 新WebSocketSession
     */
    public WebSocketSession wrap(WebSocketSession session) {
        return new ConcurrentWebSocketSessionDecorator(session,1000 * 10,1024 * 32);
    }

    /**
     * 初始化会话配置
     *
     * @param session websocket会话
     */
    public void initSessionConfig(WebSocketSession session) {
        if(session instanceof NativeWebSocketSession nativeWebSocketSession){
            if (session instanceof JettyWebSocketSession jettyWebSocketSession) {
                org.eclipse.jetty.websocket.api.Session jettySession = jettyWebSocketSession.getNativeSession();
                jettySession.setIdleTimeout(Duration.ofSeconds(90));
            } else {
                Session nativeSession = nativeWebSocketSession.getNativeSession(Session.class);
                if(nativeSession != null){
                    nativeSession.setMaxIdleTimeout(90 * 1000);
                    nativeSession.getAsyncRemote().setSendTimeout(10 * 1000);
                }
            }
        }
        session.setTextMessageSizeLimit(16 * 1024);
        session.setBinaryMessageSizeLimit(32 * 1024);
    }

    /**
     * 上线
     *
     * @param session websocket会话
     */
    public void online(WebSocketSession session) {

    }

    /**
     * 获取客户端信息
     *
     * @param session websocket会话
     * @return 客户端信息
     */
    public Principal getClientInfo(WebSocketSession session) {
        return session.getPrincipal();
    }

    /**
     * 上线成功事件
     *
     * @param session websocket会话
     */
    public void onlineSuccessEvent(WebSocketSession session) {
        if (log.isDebugEnabled()) log.debug("上线成功，客户端：{}，信息：{}", session, getClientInfo(session));
    }

    /**
     * 上线失败事件
     *
     * @param session websocket会话
     * @param status  异常原因状态代码
     */
    public void onlineFailEvent(WebSocketSession session, CloseStatus status) {
        if (log.isDebugEnabled()) log.debug("上线失败，原因：{}，客户端：{}", status, session);
    }

}
