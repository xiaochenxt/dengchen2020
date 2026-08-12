---
name: websocket-server
description: WebSocket服务端消息处理器，支持单机和集群模式，自动管理用户会话，支持向指定用户/租户/全体广播消息。当用户提到WebSocket服务端、@WebSocketMapping、SingletonDcWebSocketHandler、ClusterDcWebSocketHandler、WebSocket在线用户、推送消息等关键词时使用
---

# WebSocket 服务端消息处理器

## 概述

`dc-spring-boot-starter-websocket` 模块提供 WebSocket 服务端能力，支持单机与集群两种部署模式。继承预定义的处理器即可获得自动认证、会话管理、消息广播等能力。

| 处理器 | 适用场景 | 特点 |
|--------|---------|------|
| `SingletonDcWebSocketHandler` | 单机部署 | 本地会话管理，用户/租户维度消息发送 |
| `ClusterDcWebSocketHandler` | 多节点部署 | 基于 Redis 发布订阅跨节点广播 |

## 使用场景

- 实时消息推送（通知、订单状态）
- 在线客服系统
- 协作编辑实时同步
- 股票/行情实时推送

## 使用示例

### 定义处理器

```java
@WebSocketMapping("/ws/notify")  // 映射路径
public class NotifyWebSocketHandler extends SingletonDcWebSocketHandler {

    @Override
    protected void online(WebSocketSession session, Principal principal) {
        // 上线处理
    }

    @Override
    protected void clear(WebSocketSession session) {
        // 下线清理
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 处理客户端消息
    }

    @Override
    public int allowSameUserMaxOnlineCount() {
        return 3; // 同一用户最大在线连接数，默认 1
    }
}
```

### 单机模式发送消息

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
// 发送二进制消息
notifyWebSocketHandler.send("userId", ByteBuffer.wrap(data));
// 关闭用户连接
notifyWebSocketHandler.close("userId", CloseStatus.POLICY_VIOLATION);
```

### 集群模式

依赖 Redis，通过发布订阅跨节点广播：

```java
@WebSocketMapping("/ws/notify")
public class NotifyWebSocketHandler extends ClusterDcWebSocketHandler {
    // 继承后 send/close 方法自动跨节点广播
}
```

## 实现原理

```
继承体系：
AbstractDcWebSocketHandler (基础抽象)
  └── 自动认证 → afterConnectionEstablished()
       ├── 获取 Principal → null 则拒绝连接
       └── 调用 online() → 记录会话 → 放行
  └── 会话配置 → 心跳超时、消息大小限制
  └── 消息处理 → handleTextMessage/BinaryMessage/PongMessage
  └── 连接关闭 → clear() 清理会话映射

SingletonDcWebSocketHandler
  ├── ConcurrentHashMap<String, Queue<Session>> userIdSessionMap
  ├── ConcurrentHashMap<Long, Queue<Session>> tenantIdSessionMap
  └── 同一用户最大连接控制

ClusterDcWebSocketHandler
  └── Redis PubSub → 跨节点广播 send/close 操作
       └── WebSocketTemplate.publish(channel, message)
```

### 认证流程

```
客户端连接
    ↓
afterConnectionEstablished()
    ↓ 从 session.getPrincipal() 获取认证信息
    ↓ 为 null 或 AnonymousAuthentication
    ↓                           ↓ 是
POLICY_VIOLATION 关闭连接       初始化会话配置
    ↓                           ↓
    online(session, principal) 记录会话
    ↓
onlineSuccessEvent()
```

## 模块结构

```
dc-spring-boot-starter-websocket/src/main/java/io/github/dengchen2020/websocket/
├── annotation/
│   └── WebSocketMapping.java              // 映射注解
├── config/
│   └── SpringWebSocketAutoConfiguration.java  // 自动配置
├── handler/
│   ├── DcWebSocketHandler.java            // 基础接口
│   ├── AbstractDcWebSocketHandler.java    // 抽象处理器
│   ├── SingletonDcWebSocketHandler.java   // 单机处理器
│   └── cluster/
│       ├── ClusterDcWebSocketHandler.java // 集群处理器
│       ├── ClusterWebSocketMsgListener.java
│       ├── WebSocketSendParam.java
│       └── WebSocketTemplate.java         // 消息广播模板
├── client/
│   └── WebSocketClientUtils.java          // 客户端工具
└── properties/
    └── WebSocketProperties.java           // 配置属性
```

## 注意事项

1. WebSocket 连接依赖于上层的安全认证模块提供 `Principal` 信息，连接时会校验认证信息，未认证的连接会被拒绝
2. 集群模式 (`ClusterDcWebSocketHandler`) 需要引入 `spring-boot-starter-data-redis` 依赖
3. 心跳保活通过系统属性 `-Ddc.websocket.keepalive.enabled=true` 启用
4. `@WebSocketMapping` 的值同时也是 Spring Bean 的名称，不可重复
