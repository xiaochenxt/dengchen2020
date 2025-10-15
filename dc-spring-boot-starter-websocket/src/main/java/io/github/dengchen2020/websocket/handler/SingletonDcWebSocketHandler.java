package io.github.dengchen2020.websocket.handler;

import io.github.dengchen2020.core.security.principal.Authentication;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * 单实例服务器的websocket消息处理器
 *
 * @author xiaochen
 * @since 2024/6/26
 */
public class SingletonDcWebSocketHandler extends AbstractDcWebSocketHandler {

    protected final Map<String, ConcurrentLinkedQueue<WebSocketSession>> userIdSessionMap = new ConcurrentHashMap<>();

    protected final Map<Long, ConcurrentLinkedQueue<WebSocketSession>> tenantIdSessionMap = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService scheduledExecutorService;

    private static final String WEBSOCKET_KEEPALIVE_ENABLED = "dc.websocket.keepalive.enabled";

    static {
        if (Objects.equals(System.getProperty(WEBSOCKET_KEEPALIVE_ENABLED), Boolean.TRUE.toString())) {
            scheduledExecutorService = Executors.newScheduledThreadPool(1, Thread.ofVirtual().name("websocket-keepalive-").factory());
        }else {
            scheduledExecutorService = null;
        }
    }

    public SingletonDcWebSocketHandler() {
        if (scheduledExecutorService != null) scheduledExecutorService.scheduleAtFixedRate(this::sendPingToAll, 30, 60, TimeUnit.SECONDS);
    }

    @Override
    protected void clear(WebSocketSession session) {
        Authentication authentication = getClientInfo(session);
        if (authentication == null) return;
        if (authentication.getUserId() != null) {
            userIdSessionMap.computeIfPresent(authentication.getUserId(), (userId, sessions) -> {
                sessions.removeIf(s -> s.getId().equals(session.getId()));
                return sessions.isEmpty() ? null : sessions;
            });
        }
        if (authentication.getTenantId() != null) {
            tenantIdSessionMap.computeIfPresent(authentication.getTenantId(), (tenantId, sessions) -> {
                sessions.removeIf(s -> s.getId().equals(session.getId()));
                return sessions.isEmpty() ? null : sessions;
            });
        }
    }

    @Override
    public void online(WebSocketSession session) {
        Authentication authentication = getClientInfo(session);
        if (authentication.getUserId() == null) return;
        ConcurrentLinkedQueue<WebSocketSession> sessionQueue = userIdSessionMap.computeIfAbsent(authentication.getUserId(), (userId) -> new ConcurrentLinkedQueue<>());
        int onlineCount = sessionQueue.size();
        int allowSameUserMaxOnlineCount = allowSameUserMaxOnlineCount();
        if (onlineCount > 0 && onlineCount >= allowSameUserMaxOnlineCount){
            close(sessionQueue.iterator().next(), CloseStatus.POLICY_VIOLATION.withReason("该用户同时在线数量超过"+ allowSameUserMaxOnlineCount));
        }
        sessionQueue.add(session);
        if (authentication.getTenantId() != null) {
            tenantIdSessionMap.computeIfAbsent(authentication.getTenantId(), (tenantId) -> new ConcurrentLinkedQueue<>()).add(session);
        }
    }

    @Override
    public Authentication getClientInfo(WebSocketSession session) {
        return (Authentication) super.getClientInfo(session);
    }

    /**
     * 允许同一用户的最大连接数
     */
    public int allowSameUserMaxOnlineCount(){
        return 1;
    }

    /**
     * 关闭连接
     * @param userId 用户id
     * @param closeStatus 关闭原因
     */
    public void close(String userId, CloseStatus closeStatus){
        if (userId == null) return;
        ConcurrentLinkedQueue<WebSocketSession> sessions = userIdSessionMap.get(userId);
        if (sessions != null) sessions.forEach((session) -> close(session, closeStatus));
    }

    /**
     * 关闭连接
     * @param userId 用户id
     */
    public void close(String[] userId, CloseStatus closeStatus){
        if (userId == null) return;
        for (String s : userId) {
            ConcurrentLinkedQueue<WebSocketSession> sessions = userIdSessionMap.get(s);
            if (sessions != null) sessions.forEach((session) -> close(session, closeStatus));
        }
    }

    /**
     * 关闭租户下所有用户的连接
     * @param tenantId 租户id
     * @param closeStatus 关闭原因
     */
    public void close(Long tenantId, CloseStatus closeStatus){
        if (tenantId == null) return;
        Queue<WebSocketSession> sessions = tenantIdSessionMap.get(tenantId);
        if (sessions != null) sessions.forEach(session -> close(session, closeStatus));
    }

    /**
     * 向用户发送文本消息
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    public void send(String userId, String message) {
        if (userId == null || message == null) return;
        ConcurrentLinkedQueue<WebSocketSession> sessions = userIdSessionMap.get(userId);
        if (sessions != null) sessions.forEach((session) -> send(session, message));
    }

    /**
     * 向用户发送文本消息
     *
     * @param userId 用户id
     * @param message 文本消息
     */
    public void send(String[] userId, String message) {
        if (userId == null || message == null) return;
        for (String s : userId) {
            ConcurrentLinkedQueue<WebSocketSession> sessions = userIdSessionMap.get(s);
            if (sessions != null) sessions.forEach((session) -> send(session, message));
        }
    }

    /**
     * 向用户发送文本消息
     *
     * @param tenantId 租户id
     * @param message  文本消息
     */
    public void send(Long tenantId, String message) {
        if (tenantId == null || message == null) return;
        Queue<WebSocketSession> sessions = tenantIdSessionMap.get(tenantId);
        if (sessions != null) sessions.forEach(session -> send(session, message));
    }

    /**
     * 向所有用户发送文本消息
     *
     * @param message 文本消息
     */
    public void sendToAll(String message) {
        if (message == null) return;
        // 向所有连接的客户端发送消息
        userIdSessionMap.forEach((a, sessions)
                -> sessions.forEach((session) -> send(session, message)));
    }

    /**
     * 向用户发送二进制消息
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    public void send(String userId, ByteBuffer message) {
        if (userId == null || message == null) return;
        ConcurrentLinkedQueue<WebSocketSession> sessions = userIdSessionMap.get(userId);
        if (sessions != null) sessions.forEach((session) -> send(session, message));
    }

    /**
     * 向用户发送二进制消息
     *
     * @param userId 用户id
     * @param message 二进制消息
     */
    public void send(String[] userId, ByteBuffer message) {
        if (userId == null || message == null) return;
        for (String s : userId) {
            ConcurrentLinkedQueue<WebSocketSession> sessions = userIdSessionMap.get(s);
            if (sessions != null) sessions.forEach((session) -> send(session, message));
        }
    }

    /**
     * 向用户发送二进制消息
     *
     * @param tenantId 租户id
     * @param message  二进制消息
     */
    public void send(Long tenantId, ByteBuffer message) {
        if (tenantId == null || message == null) return;
        Queue<WebSocketSession> sessions = tenantIdSessionMap.get(tenantId);
        if (sessions != null) sessions.forEach(session -> send(session, message));
    }

    /**
     * 向所有用户发送二进制消息
     *
     * @param message 二进制消息
     */
    public void sendToAll(ByteBuffer message) {
        if (message == null) return;
        userIdSessionMap.forEach((a, sessions)
                -> sessions.forEach((session) -> send(session, message)));
    }

    /**
     * 向所有用户发送Ping消息
     */
    protected void sendPingToAll() {
        userIdSessionMap.forEach((a, sessions)
                -> sessions.forEach((session) -> sendPing(session)));
    }

}
