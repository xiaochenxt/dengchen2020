---
name: websocket-template
description: WebSocket跨服务消息发送模板，通过Redis广播向其他服务的WebSocket客户端推送消息，支持按用户/租户/全体发送文本和二进制消息。当用户提到WebSocketTemplate、跨服务WebSocket推送、其他服务发WebSocket消息、WebSocket跨进程通知、WebSocket广播等关键词时使用
---

# WebSocket 跨服务消息推送

## 概述

`WebSocketTemplate` 是基于 Redis 发布订阅的 WebSocket 消息发送模板，用于向 **其他服务** 的 WebSocket 客户端推送消息。与 `ClusterDcWebSocketHandler` 不同，该模板可以脱离 WebSocket 处理器单独实例化，在任意服务中使用。

典型跨服务架构：

```
App 服务（持有 WebSocket 连接）        管理后台服务（无 WebSocket）
┌────────────────────────┐          ┌────────────────────────┐
│  WebSocketHandler      │          │  WebSocketTemplate     │
│  └── 在线用户会话管理    │          │  └── 调用 send()       │
└────────┬───────────────┘          └────────┬───────────────┘
         │ 接收并推送 WebSocket 消息          │ 通过 Redis PubSub 发送
         ↓                                   ↓
         └────────── Redis 广播 ──────────────┘
                    (dc:websocket:/ws/notify)
```

| 特性 | 说明 |
|------|------|
| **独立使用** | 无需 WebSocket 处理器，任意服务中注入即可 |
| **跨服务推送** | 管理后台可向 App/商家端 WebSocket 推送消息 |
| **多路径支持** | `getInstance()` 切换不同映射路径 |
| **消息类型** | 文本、二进制、关闭连接 |

## 使用场景

- 管理后台接口向 App 端用户推送通知
- 订单服务向商家端 WebSocket 推送新订单提醒
- 定时任务服务向所有在线用户广播消息
- 微服务架构中跨服务 WebSocket 消息推送

## 使用示例

### 配置 WebSocketTemplate

```java
@Resource
private RedisMessagePublisher redisMessagePublisher;

// 在需要向其他服务 WebSocket 推送消息的地方实例化
WebSocketTemplate appWebSocket = new WebSocketTemplate(
        "/ws/app/notify",           // App 服务的 WebSocket 映射路径
        redisMessagePublisher
);
WebSocketTemplate merchantWebSocket = new WebSocketTemplate(
        "/ws/merchant/notify",       // 商家端服务的 WebSocket 映射路径
        redisMessagePublisher
);
```

### 跨服务发送消息

```java
// 管理后台接口中，向 App 端用户推送订单状态
appWebSocket.send(userId, "您的订单已发货");

// 向多个 App 端用户推送
appWebSocket.send(new String[]{"user1", "user2"}, "全员促销活动开始");

// 向 App 端租户下所有在线用户推送
appWebSocket.send(tenantId, "租户公告");

// 向 App 端全体在线用户广播
appWebSocket.sendToAll("系统维护通知");

// 向商家端 WebSocket 推送新订单提醒
merchantWebSocket.send(merchantId, "您有新订单，订单号：2024001");

// 踢用户下线
appWebSocket.close(userId, CloseStatus.POLICY_VIOLATION);
```

### 通过 getInstance 切换映射路径

```java
WebSocketHelper helper = new WebSocketTemplate("/ws/notify", redisMessagePublisher);

// 切换到其他 WebSocket 映射路径
WebSocketTemplate orderWs = helper.getInstance("/ws/order");
orderWs.send(userId, "订单消息");
```

### 发送二进制消息

```java
// 向用户发送二进制数据
appWebSocket.send(userId, ByteBuffer.wrap(binaryData));
// 全体广播二进制消息
appWebSocket.sendToAll(ByteBuffer.wrap(binaryData));
```

## 实现原理

```
WebSocketTemplate.send(userId, message)
    ↓
WebSocketSendParam.userText(userId, message)
    ↓ 构建传输参数 Record（含 userId、message、type 等）
    ↓
redisMessagePublisher.publish(topic, param)
    ↓ Redis PubSub
    ↓
持有该 WebSocket 的节点收到广播
    ↓
ClusterWebSocketMsgListener.onMessage()
    ↓ 解析 WebSocketSendParam
    ↓
ClusterDcWebSocketHandler 处理
    ├── type=1 → send(userId, message)
    ├── type=2 → send(tenantId, message)
    ├── type=3 → sendToAll(message)
    └── type=0 → close(userId, closeStatus)
```

**关键点**：`WebSocketTemplate` 只负责通过 Redis 发布消息，不关心谁消费。目标服务的 `ClusterDcWebSocketHandler` 订阅了对应 topic 的 Redis 频道，收到广播后推送 WebSocket 消息给客户端。

## 模块结构

```
dc-spring-boot-starter-websocket/src/main/java/io/github/dengchen2020/websocket/
├── handler/cluster/
│   ├── WebSocketTemplate.java          // 消息发送模板（可独立使用）
│   ├── WebSocketSendParam.java         // 传输参数 Record
│   ├── ClusterDcWebSocketHandler.java  // 集群处理器（消费广播）
│   └── ClusterWebSocketMsgListener.java// Redis 消息监听
```

## 注意事项

1. `WebSocketTemplate` 需要 `RedisMessagePublisher`，依赖 `spring-boot-starter-data-redis`
2. 映射路径需与目标服务的 `@WebSocketMapping` 值一致，大小写敏感
3. 目标服务必须使用 `ClusterDcWebSocketHandler`（或其子类）才能接收跨服务广播
4. 通过 `getInstance(mapping)` 可获取不同路径的 template 实例，避免重复创建
