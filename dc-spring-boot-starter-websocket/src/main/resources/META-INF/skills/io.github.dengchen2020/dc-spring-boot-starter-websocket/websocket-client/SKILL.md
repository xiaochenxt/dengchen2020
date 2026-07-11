---
name: websocket-client
description: WebSocket客户端工具，支持创建会话、自动重连、并发安全包装。当用户提到WebSocket客户端、WebSocketClientUtils、创建WebSocket会话、自动重连、ws客户端等关键词时使用
---

# WebSocket 客户端工具

## 概述

`dc-spring-boot-starter-websocket` 模块的 `WebSocketClientUtils` 提供 WebSocket 客户端连接能力，支持自动适配 Jetty/Tomcat/Undertow 容器、自动重连、并发安全包装。

| 特性 | 说明 |
|------|------|
| **容器适配** | 自动检测 Jetty / Tomcat / Undertow，优化线程模型 |
| **并发安全** | 使用 `ConcurrentWebSocketSessionDecorator` 包装 |
| **自动重连** | 掉线后按策略自动重连，可配置最大重连次数 |

## 使用场景

- 连接外部 WebSocket 服务（行情推送、实时消息）
- 服务间 WebSocket 通信
- 需要掉线自动重连的 WebSocket 客户端

## 使用示例

### 创建基本会话

```java
WebSocketSession session = WebSocketClientUtils.createSession(
    "ws://localhost:8080/ws/notify",
    new WebSocketHandler() {
        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            // 连接建立
        }
        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) {
            // 处理消息
        }
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            // 连接关闭
        }
    }
);
```

### 创建带自动重连的会话

```java
// 掉线后自动重连，最多重连 5 次
WebSocketSession session = WebSocketClientUtils.createSession(
    "ws://localhost:8080/ws/notify", webSocketHandler, 5);
```

### 发送消息

```java
// 发送文本消息
session.sendMessage(new TextMessage("你好"));

// 发送二进制消息
session.sendMessage(new BinaryMessage(ByteBuffer.wrap(data)));
```

## 实现原理

```
WebSocketClientUtils.createSession(url, handler)
    ↓
createClient() → StandardWebSocketClient
    ↓ 自动适配 WebSocketContainer
    ├── Jetty  → JakartaWebSocketClientContainerProvider（虚拟线程执行器）
    ├── Tomcat → WsWebSocketContainer（虚拟线程 AsynchronousChannelGroup）
    └── 其他   → ContainerProvider.getWebSocketContainer()
    ↓
client.execute(handler, headers, URI)
    ↓
ConcurrentWebSocketSessionDecorator.wrap(session)  // 并发安全包装
```

### 自动重连原理

```
createSession(url, handler, maxReconnectAttempts)
    ↓
返回 WebSocketSessionReconnectSupport 代理
    ↓
afterConnectionClosed 时判断
    ├── CloseStatus.NORMAL → 不重连
    └── 其他异常关闭 → 虚拟线程执行重连逻辑
```

## 模块结构

```
dc-spring-boot-starter-websocket/src/main/java/io/github/dengchen2020/websocket/
├── client/
│   └── WebSocketClientUtils.java      // 客户端工具
│       └── WebSocketSessionReconnectSupport  // 自动重连内部类
└── ...
```

## 注意事项

1. 创建会话后需由调用方负责关闭，推荐在 `finally` 块或 `@PreDestroy` 中关闭
2. 自动重连仅在非正常关闭时触发（`CloseStatus.NORMAL` 不触发重连）
3. 线程模型优化：Jetty 使用虚拟线程执行器，Tomcat 使用虚拟线程的 AsynchronousChannelGroup，避免传统 WebSocket 客户端导致的线程数暴涨
4. 返回的 `WebSocketSession` 已通过 `ConcurrentWebSocketSessionDecorator` 包装，支持并发发送消息
