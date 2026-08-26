# dengchen2020

一套基于 **Spring Boot 3 / Java 25** 的企业级开发增强组件库，涵盖缓存、ID 生成、限流、安全认证、消息通知、WebSocket、JPA 增强、IP 归属地、分布式锁、原生镜像等常用能力，开箱即用。

- groupId：`io.github.dengchen2020`
- 各模块均为标准 Spring Boot Starter，按需引入依赖即可自动装配

## 模块一览

| 模块 | 说明 |
|------|------|
| dc-spring-boot-starter-core | 公共基础设施：分页、认证上下文、定时任务、RabbitMQ 增强、Redis 发布订阅、CORS、ETag |
| dc-spring-boot-starter-cache | Caffeine/Redis 缓存增强，本地缓存多节点同步清除 |
| dc-spring-boot-starter-id-generator | 雪花算法 ID（多节点自动分配机器码）、Redis 递增 ID |
| dc-spring-boot-starter-jpa | Querydsl 类型安全 JPA：DTO 投影、悲观锁、租户隔离、分页 |
| dc-spring-boot-starter-security | Token 认证（JWT 无状态 / Redis 有状态单设备·多设备）、权限校验 |
| dc-spring-boot-starter-lock | 分布式锁（Redisson），注解 + 编程两种方式 |
| dc-spring-boot-starter-ratelimiter | 限流：单机滑动窗口/令牌桶，Redis 分布式限流 |
| dc-spring-boot-starter-message | 钉钉、飞书、企业微信机器人消息推送，邮件发送 |
| dc-spring-boot-starter-websocket | WebSocket 单机/集群模式，跨服务消息推送 |
| dc-spring-boot-starter-ip | IP 归属地查询（ip2region xdb，支持 IPv4/IPv6） |
| dc-spring-boot-native-image | GraalVM 原生镜像编译支持 |
| dc-utils | 工具类集合：JSON、Bean 拷贝、加解密、验证码、二维码、CSV、S3 等 |
| jackson-ext | Jackson 扩展：`@JsonRawValue` 反序列化支持 |
| dc-dependencies / dc-spring-boot-starter-parent | 依赖版本统一管理的 BOM 与 Parent |

---

## 典型示例

### 缓存（dc-spring-boot-starter-cache）

```properties
spring.cache.type=caffeine
dc.cache.caffeine.specs.userCache.expire-time=90s
dc.cache.caffeine.specs.userCache.max=1000
```

使用 Caffeine 时若引入了 `spring-boot-starter-data-redis`，`@CacheEvict` 会通过 Redis 发布订阅自动清除其他节点的本地缓存。编程方式：

```java
@Resource CacheHelper cacheHelper;
cacheHelper.evict("cacheName", "key"); // 移除指定 key
cacheHelper.clear("cacheName");        // 清空缓存名
```

### ID 生成（dc-spring-boot-starter-id-generator）

引入 `spring-boot-starter-data-redis` 后多节点自动分配机器码，零配置直接使用：

```java
IdHelper.nextId();               // 全局唯一 ID（long）
IdHelper.nextIdBase62();         // 62 进制字符串
IdHelper.extractTime(id);        // 从 ID 解析时间
```

### JPA 增强（dc-spring-boot-starter-jpa）

DTO/Record 投影，实体同名字段自动查询，加字段后查询零改动：

```java
@Repository
public interface UserRepository extends BaseJpaRepository<User, Long> {}

public record UserInfo(Long id, String name, String phone) {}

List<UserInfo> list = selectRecord(UserInfo.class)
        .where(q_user.status.eq(1))
        .fetch();
```

连表分页：

```java
if (StringUtils.hasText(param.getName())) builder.and(q_order.firstName.eq(param.getName()));
if (param.getPayStartTime() != null && param.getPayEndTime() != null) builder.and(q_order.payTime.between(param.getPayStartTime(), param.getPayEndTime()));
var query = selectBean(OrderDTO.class, q_user.name.as("userName"))
        .leftJoin(q_user).on(q_order.userId.eq(q_user.id))
        .where(builder);
return fetchPage(query, pageParam, q_order.id.desc());
```

悲观锁一行搞定：`selectByIdForUpdate(id)`、`selectInIdsForUpdateSkipLocked(ids)`。

### 安全认证（dc-spring-boot-starter-security）

```properties
dc.security.authentication-type=com.example.Authentication
dc.security.jwt.secret=your-secret-key   # 配置后启用 JWT 无状态模式
```

```java
tokenService.createToken(Authentication.create("userId")); // 签发 Token

@NoTokenRequired                          // 该接口无需认证
@GetMapping("/login")
public Result login() { ... }

@HasPermission({"admin", "order:read"})   // 拥有任一权限即可访问
@GetMapping("/admin/orders")
public Result listOrders() { ... }
```

Redis 有状态模式支持单设备顶替下线、多设备最大在线数控制、自动续期、踢人下线。

### 分布式锁（dc-spring-boot-starter-lock）

```java
@Lock(value = "#id", name = "order", waitTime = 3, errorMsg = "系统繁忙，请稍后再试")
public void processOrder(Long id) { ... }

// 编程方式
dLock.tryLockAndRun("order:" + id, () -> { ... });
```

### 限流（dc-spring-boot-starter-ratelimiter）

按是否引入 Redis 依赖自动切换本地/分布式限流：

```java
@RateLimit(value = 60, time = 1, timeUnit = TimeUnit.MINUTES,
        strategy = RateLimitStrategy.userAndUri, errorMsg = "请求过于频繁")
@GetMapping("/list")
public Result list() { ... }
```

### 消息通知（dc-spring-boot-starter-message）

```java
dingTalkClient.send(new DingTalkClient.MarkdownMessage("告警", "# 服务异常"));
feiShuClient.send(new FeiShuClient.TextMessage("你好，世界").addAtAll());
weChatClient.send(new WeChatClient.TextMessage("你好，世界", "138xxxx0001"));
emailClient.sendMime("主题", "<h1>HTML 内容</h1>", new FileDataSource("/path/file.pdf"));
```

### WebSocket（dc-spring-boot-starter-websocket）

```java
@WebSocketMapping("/ws/notify")
public class NotifyWebSocketHandler extends SingletonDcWebSocketHandler {
    // 集群模式继承 ClusterDcWebSocketHandler 即可（依赖 Redis 广播）
}

@Resource NotifyWebSocketHandler handler;
handler.send("userId", "你好");        // 发给指定用户
handler.sendToAll("全体通知");          // 广播
```

集群环境下还可在任意服务通过 `WebSocketTemplate` 向其他服务的客户端跨节点推送。

### IP 归属地（dc-spring-boot-starter-ip）

```java
@Resource IpService ipService;
IpInfo info = ipService.getInfo("114.114.114.114");
info.country();  // 中国
info.province(); // 江苏
info.city();     // 南京
```

### 公共基础设施（dc-spring-boot-starter-core）

定时任务多机互斥执行（无需引入 Redis 之外的组件，默认独占 20 秒）：

```java
@DcScheduled(cron = "0 0 2 * * ?", seconds = 60) // 60 秒内仅一台执行
public void nightlyTask() { ... }
```

RabbitMQ 延迟消息：

```java
rabbitDelayTemplate.send(RK_ORDER_TIMEOUT_CLOSE, orderId, Duration.ofMinutes(10));
```

Redis 发布订阅（注解订阅，支持通配符）：

```java
publisher.publish("channel:name", object);

@RedisListener("order:*")
public void onPattern(String message) { ... }
```

### 工具类（dc-utils）

```java
JsonUtils.toJson(source);                          // JSON 序列化
BeanUtils.copyProperties(source, target);          // CGLib Bean 拷贝，忽略 null
String code = RandomStringUtils.insecure().nextNumeric(6); // 随机数字验证码
CaptchaUtils.arithmetic();                         // 算术验证码
QRCodeGenerator.builder().size(300).logo(logoBytes).build(); // 二维码
```

### jackson-ext

为 `@JsonRawValue` 补充反序列化支持，数据库 JSON 字段映射为 String 时读写均保持 JSON 原样：

```java
@JsonRawValue
private String attributes; // 序列化不转义，反序列化直接接收 JSON 对象/数组
```

### 原生镜像（dc-spring-boot-native-image）

引入依赖后，先执行 `spring-boot:process-aot`，再执行 `native:compile-no-fork` 即可完成 GraalVM 原生镜像编译。

---

## 引入方式

各模块独立发布，按需引入，例如：

```xml
<dependency>
    <groupId>io.github.dengchen2020</groupId>
    <artifactId>dc-spring-boot-starter-cache</artifactId>
    <version>${lastVersion}</version>
</dependency>
```

更多详细用法请参见各模块目录下的 README.md。

## License

[Apache License 2.0](LICENSE)
