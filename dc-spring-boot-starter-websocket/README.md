WebSocket 支持，包含服务端消息处理和客户端连接工具，服务端支持单机与集群模式。

服务端处理器继承体系：

```
AbstractDcWebSocketHandler  (基础抽象，自动认证、会话配置)
  └── SingletonDcWebSocketHandler   (单实例，用户/租户会话管理)
       └── ClusterDcWebSocketHandler  (集群，依赖Redis广播)
```

定义服务端处理器：

```java
@WebSocketMapping("/ws/notify")  // 映射路径，同时作为Bean名称
public class NotifyWebSocketHandler extends SingletonDcWebSocketHandler {

    @Override
    protected void online(WebSocketSession session, Principal principal) {
        // 上线处理，可在此记录日志或扩展业务
    }

    @Override
    protected void clear(WebSocketSession session) {
        // 下线清理资源
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理客户端消息
        log.info("收到消息：{}", message);
    }

    @Override
    public int allowSameUserMaxOnlineCount() {
        return 3; // 同一用户最大在线连接数，默认1
    }
}
```

**单机模式** 向用户发送消息：

```java
@Resource
private NotifyWebSocketHandler notifyWebSocketHandler;

// 向指定用户发送文本消息
notifyWebSocketHandler.send("userId", "你好");
// 向多个用户发送
notifyWebSocketHandler.send(new String[]{"user1", "user2"}, "通知内容");
// 向租户下所有用户发送
notifyWebSocketHandler.send(10001L, "租户通知");
// 向所有在线用户广播
notifyWebSocketHandler.sendToAll("全体通知");
// 向用户发送二进制消息
notifyWebSocketHandler.send("userId", ByteBuffer.wrap(data));
// 关闭用户连接
notifyWebSocketHandler.close("userId", CloseStatus.POLICY_VIOLATION);
```

**集群模式**（引入 `spring-boot-starter-data-redis`，通过 Redis 发布订阅跨节点广播）：

```java
@WebSocketMapping("/ws/notify")
public class NotifyWebSocketHandler extends ClusterDcWebSocketHandler {
    // 同上，send/close 方法自动跨节点广播
}
```

**跨服务消息推送**（`WebSocketTemplate`）— 在任意服务中向其他服务的 WebSocket 客户端推送消息：

```java
@Resource
private RedisMessagePublisher redisMessagePublisher;

// 实例化 WebSocketTemplate，指向目标服务的映射路径
WebSocketTemplate appWebSocket = new WebSocketTemplate(
        "/ws/app/notify",          // App 服务的 WebSocket 路径
        redisMessagePublisher
);
WebSocketTemplate merchantWebSocket = new WebSocketTemplate(
        "/ws/merchant/notify",      // 商家端服务的 WebSocket 路径
        redisMessagePublisher
);

// 管理后台向 App 端用户推送消息
appWebSocket.send(userId, "您的订单已发货");
appWebSocket.send(new String[]{"user1", "user2"}, "通知");
appWebSocket.send(tenantId, "租户公告");
appWebSocket.sendToAll("全体通知");
appWebSocket.close(userId, CloseStatus.POLICY_VIOLATION);

// 向商家端推送新订单提醒
merchantWebSocket.send(merchantId, "您有新订单");
```

> `WebSocketTemplate` 可独立于 WebSocket 处理器使用，通过 Redis 广播 → 目标服务的 `ClusterDcWebSocketHandler` 接收 → 推送 WebSocket 消息给客户端。

WebSocket 客户端工具：

```java
// 创建客户端会话
WebSocketSession session = WebSocketClientUtils.createSession(
    "ws://localhost:8080/ws/notify", webSocketHandler);

// 创建带自动重连的会话（最多重连5次）
WebSocketSession session = WebSocketClientUtils.createSession(
    "ws://localhost:8080/ws/notify", webSocketHandler, 5);

// 使用完毕需关闭
session.close();
```

properties 配置：

```properties
# 是否启用WebSocket自动配置（默认 true）
dc.websocket.enabled=true
# 允许跨域来源
dc.websocket.allowed-origins=http://localhost:3000
dc.websocket.allowed-origin-patterns=*
# 启用 SockJS 回退（默认 false）
dc.websocket.with-sock-js=false
# 启用心跳保活（默认 false，需主动设置-D系统属性）
# -Ddc.websocket.keepalive.enabled=true
```
