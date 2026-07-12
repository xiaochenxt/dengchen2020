提供公共基础设施：分页参数、认证上下文、定时任务、RabbitMQ 增强、Redis 消息发布订阅、CORS、ETag启用

---
分页查询（Page / PageParam）：

```java
// 控制器接收分页参数
public Result list(PageParam param) { ... }

// 手动构建分页
Page page = Page.of(1, 10);
Page page = Page.of(1, 10, false); // 不查总条数
```

认证上下文（SecurityContextHolder）：

```java
// 获取当前登录用户
Authentication auth = SecurityContextHolder.getAuthentication();
String userId = auth.userId();

// 获取租户信息
if (auth instanceof TenantInfo tenant) {
    Long tenantId = tenant.tenantId();
}
```

定时任务（Scheduled）— 支持多服务器互斥执行：

```java
@Scheduled(cron = "0 0 2 * * ?")
// 多出的能力：默认同一时间只允许一台服务器独占执行，默认独占执行时间为20秒，20秒后继续争抢独占执行权。例如每5秒打印一次hello world，20秒内A服务会执行4次，B服务一次都不会执行（如果20秒内A服务停止，B服务会接替执行）
public void nightlyTask() {
    // 业务逻辑
}

@DcScheduled(cron = "0 0/5 * * * ?", concurrency = true) // 允许多台同时执行
@DcScheduled(cron = "0 0 2 * * ?", seconds = 60)         // 60秒内只有其中一台服务器拥有独占执行权
```

RabbitMQ 增强，减少模板代码，使用方式超级简单：

```java
// 自动配置：消息转换器、确认回调、退回回调、死信队列、延迟交换机
// 延迟消息发送
@Resource
private RabbitDelayTemplate rabbitDelayTemplate;

rabbitDelayTemplate.send(RK_ORDER_TIMEOUT_CLOSE, message, Duration.ofMinutes(10));
```

消费延迟消息（使用 `RabbitConstant.DELAY_EXCHANGE` 延迟交换机）：

```java
static final String QUEUE_ORDER_TIMEOUT_CLOSE = "order_timeout_close";
public static final String RK_ORDER_TIMEOUT_CLOSE = "order.timeout.close";

@RabbitListener(concurrency = "1-4", bindings = @QueueBinding(
        value = @Queue(QUEUE_ORDER_TIMEOUT_CLOSE),
        key = RK_ORDER_TIMEOUT_CLOSE,
        exchange = @Exchange(value = RabbitConstant.DELAY_EXCHANGE, delayed = Exchange.TRUE)))
public void consumer(String orderId, Message message, Channel channel) {
    orderService.timeoutClose(orderId);
}
```

Redis 消息发布订阅：

```java
// 发布消息
@Resource
private RedisMessagePublisher publisher;

publisher.publish("channel:name", object);     // JSON序列化
publisher.publish("channel:name", "字符串消息");

// 订阅消息
@Component
public class MyListener {
    @RedisListener("channel:name")
    public void onMessage(MyEvent event) {
        // 处理消息
    }

    @RedisListener("order:*")  // 支持通配符
    public void onPattern(String message) { }
}
```

CORS 跨域配置：

```properties
dc.cors.enabled=true # 默认启用
dc.cors.allowed-origins=http://localhost:3000
dc.cors.allowed-origin-patterns=*
dc.cors.allow-credentials=true
dc.cors.max-age=3600s
```

ETag 静态资源配置：

```properties
dc.etag.enabled=true
```
