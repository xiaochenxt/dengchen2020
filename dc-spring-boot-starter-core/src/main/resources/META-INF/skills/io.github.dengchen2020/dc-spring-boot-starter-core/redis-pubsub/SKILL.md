---
name: redis-pubsub
description: Redis消息发布订阅，支持JSON序列化、通配符订阅。当用户提到Redis发布订阅、RedisMessagePublisher、@RedisListener、Redis PubSub、Redis消息、Redis广播等关键词时使用
---

# Redis 消息发布订阅

## 概述

`dc-spring-boot-starter-core` 模块提供基于 Redis 的异步消息发布订阅能力。支持 JSON 序列化、通配符模式订阅等特性。

| 特性 | 说明                                             |
|------|------------------------------------------------|
| **发布** | 支持对象(JSON)、字符串、字节数组三种消息类型                      |
| **订阅** | `@RedisListener` 注解，支持频道和通配符模式                 |
| **序列化** | 默认使用 `GenericJackson2JsonRedisSerializer`，含类型信息 |

## 使用场景

- 微服务间异步通知（配置更新、缓存失效）
- WebSocket 集群跨节点消息广播
- 异步事件处理（订单状态变更通知）

## 使用示例

### 发布消息

```java
@Resource
private RedisMessagePublisher publisher;

// 发布对象消息（JSON序列化）
publisher.publish("channel:order", orderEvent);

// 发布字符串消息
publisher.publish("channel:notify", "订单已支付");

// 发布字节数组消息
publisher.publish("channel:data", byteData);
```

### 订阅消息

```java
@Component
public class OrderEventListener {

    // 订阅指定频道
    @RedisListener("channel:order")
    public void onOrderEvent(OrderEvent event) {
        // 处理订单事件
        orderService.handle(event);
    }

    // 订阅通配符模式（匹配 channel:order.created, channel:order.paid 等）
    @RedisListener("channel:order.*")
    public void onOrderPattern(String message) {
        // 通配符订阅只支持 String 参数
    }

    // 订阅字节数组消息
    @RedisListener("channel:data")
    public void onBytes(byte[] data) {
        // 处理二进制数据
    }
}
```

## 实现原理

### 发布流程

```
RedisMessagePublisher.publish(channel, message)
    ↓
serializer.serialize(message)  →  默认 GenericJackson2JsonRedisSerializer（含 @class 类型信息）
    ↓
ReactiveRedisTemplate.convertAndSend(channel, bytes)
    ↓ 失败重试（指数退避，3次）
    ↓
Redis PubSub → 所有订阅该频道的节点
```

### 订阅注册

```
应用启动
    ↓
BeanPostProcessor: RedisMessageListenerRegistrar
    ↓ 扫描所有 Bean 中带 @RedisListener 注解的方法
    ↓
MessageListenerAdapter(bean, methodName)
    ↓ 根据参数类型选择序列化器
    ├── byte[].class → RedisSerializer.byteArray()
    ├── String.class → RedisSerializer.string()
    └── 其他类型     → GenericJackson2JsonRedisSerializer
    ↓
RedisMessageListenerContainer.addMessageListener(adapter, topic)
```

### 频道 vs 通配符

| 订阅类型 | 示例 | 匹配规则 |
|---------|------|---------|
| **频道(ChannelTopic)** | `@RedisListener("channel:order")` | 精确匹配 |
| **通配符(PatternTopic)** | `@RedisListener("channel:order.*")` | 支持 `*`、`?`、`[...]` 通配符 |

判断逻辑：包含 `*`、`?`、`[` 中任一字符且未被转义时视为通配符，否则视为精确频道。

## 注意事项

1. 依赖 `spring-boot-starter-data-redis`，订阅时通过 `RedisMessageListenerContainer` 监听
2. 通配符订阅的方法参数类型只能为 `String` 或 `byte[]`，精确频道订阅支持任意类型（JSON 自动反序列化）
3. `GenericJackson2JsonRedisSerializer` 带 `@class` 类型信息，反序列化时需保证 class 存在且兼容
