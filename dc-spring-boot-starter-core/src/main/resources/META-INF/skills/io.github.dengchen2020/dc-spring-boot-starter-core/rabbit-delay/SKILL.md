---
name: rabbit-delay
description: RabbitMQ延迟消息，基于x-delayed-message插件实现，提供延迟队列声明和消息发送。当用户提到RabbitMQ延迟消息、延迟队列、RabbitDelayTemplate、延迟交换机、x-delayed-message、延时消息等关键词时使用
---

# RabbitMQ 延迟消息

## 概述

`dc-spring-boot-starter-core` 模块提供 RabbitMQ 延迟消息能力，基于 `x-delayed-message` 交换机类型实现。自动配置延迟交换机、死信队列、消息转换器和回调。

| 特性 | 说明 |
|------|------|
| **延迟交换机** | 自动声明 `delay.exchange`（Direct 类型，x-delayed-message） |
| **延迟发送** | `RabbitDelayTemplate` 支持指定延迟时长 |
| **消息确认** | 自动配置 ConfirmCallback 和 ReturnsCallback |
| **死信队列** | 自动声明死信交换机和死信队列 |

## 使用场景

- 订单超时未支付自动取消
- 定时任务延迟触发
- 消息重试延迟处理
- 业务延迟通知

## 使用示例

### 发送延迟消息

```java
@Resource
private RabbitDelayTemplate rabbitDelayTemplate;

// 延迟 10 分钟
rabbitDelayTemplate.send(RK_ORDER_TIMEOUT, orderId, Duration.ofMinutes(10));
```

### 消费延迟消息

```java
public static final String QUEUE_ORDER_TIMEOUT = "queue_order_timeout";
public static final String RK_ORDER_TIMEOUT = "order.timeout";

@RabbitListener(concurrency = "1-4", bindings = @QueueBinding(
        value = @Queue(QUEUE_ORDER_TIMEOUT),
        key = RK_ORDER_TIMEOUT,
        exchange = @Exchange(
                value = RabbitConstant.DELAY_EXCHANGE,
                delayed = Exchange.TRUE)))
public void consumer(String orderId, Message message, Channel channel) {
    orderService.timeoutClose(orderId);
}
```

## 实现原理

```
消息发送流程：

RabbitDelayTemplate.send(routingKey, message, delay)
    ↓
rabbitTemplate.convertAndSend(DELAY_EXCHANGE, routingKey, message, msg -> {
    msg.getMessageProperties().setDelayLong(delay.toMillis());
    return msg;
})
    ↓
延迟交换机 (x-delayed-message)
    ↓ 等待延迟时间到达
    ↓
根据 x-delayed-type 类型投递到绑定队列
    ↓
消费者消费
```

### 自动配置

```
RabbitAutoConfiguration 自动注册：
├── Jackson2JsonMessageConverter     // 消息转换器
├── ConfirmCallback                  // 发送确认回调
├── ReturnsCallback                  // 退回回调
├── MessageRecoverer                 // 消费失败处理
├── deadLetterExchange/Queue/Binding // 死信队列
├── delayExchange                    // 延迟交换机 (x-delayed-message)
└── RabbitDelayTemplate              // 延迟消息发送模板
```

## 模块结构

```
dc-spring-boot-starter-core/src/main/java/io/github/dengchen2020/core/rabbit/
├── RabbitAutoConfiguration.java      // RabbitMQ 自动配置
├── RabbitConstant.java               // 常量（交换机/队列/路由键名称）
├── RabbitDelayTemplate.java          // 延迟消息发送模板
└── JacksonJsonMessageConverter.java // JSON 消息转换器
```

## 注意事项

1. 延迟消息依赖 RabbitMQ `x-delayed-message` 插件，需提前在 RabbitMQ 中安装：`rabbitmq-plugins enable rabbitmq_delayed_message_exchange`
2. 延迟范围：0 ~ 2^32 毫秒（约 49 天），超出此范围的消息会立即投递
3. `concurrency` 参数控制消费者的并发数，格式为 `"min-max"`（如 `"1-4"`）
4. 死信队列用于处理消费失败的消息，配合 `MessageRecoverer` 记录失败日志并阻止重回队列
