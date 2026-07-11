单机限流和分布式限流，支持注解方式和编程方式。

根据项目中是否引入 `spring-boot-starter-data-redis` 自动切换为本地限流或 Redis 分布式限流。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

注解方式使用：

```java
@RateLimit(value = 60, time = 1, timeUnit = TimeUnit.MINUTES, errorMsg = "请求过于频繁")
@GetMapping("/list")
public Result list() {
    return Result.ok();
}
```

`@RateLimit` 支持类级别和方法级别，方法级别覆盖类级别。

限流策略：

```java
@RateLimit(strategy = RateLimitStrategy.userAndUri) // 按用户+URI限流（默认，用户不可用时回退为ip+uri）
@RateLimit(strategy = RateLimitStrategy.ipAndUri)    // 按IP+URI限流
@RateLimit(strategy = RateLimitStrategy.user)        // 按用户限流
@RateLimit(strategy = RateLimitStrategy.ip)          // 按IP限流
@RateLimit(strategy = RateLimitStrategy.uri)         // 按URI限流
```

编程方式使用（本地限流-滑动窗口）：

```java
LocalRateLimiter limiter = new LocalRateLimiter(Duration.ofSeconds(1));
if (limiter.limit("user:123", 60)) {
    throw new RuntimeException("请求过于频繁");
}
```

编程方式使用（本地限流-令牌桶）：

```java
TokenBucketRateLimiter limiter = new TokenBucketRateLimiter();

// 非阻塞尝试获取令牌
if (limiter.tryAcquire("user:123", 60)) {
    // 通过限流，执行业务逻辑
}

// 阻塞等待获取令牌
limiter.acquire("user:123", 60);

// 带超时的获取令牌
boolean acquired = limiter.tryAcquire("user:123", 60, Duration.ofMillis(500));
```

编程方式使用（分布式限流）：

```java
@Resource
private StringRedisTemplate stringRedisTemplate;

RedisRateLimiter limiter = new RedisRateLimiter(stringRedisTemplate);
if (limiter.limit("user:123", 60, Duration.ofMinutes(1))) {
    throw new RuntimeException("请求过于频繁");
}
```

properties 配置：

```properties
# 是否开启限流（默认 true）
dc.ratelimiter.enabled=true
# 默认异常提示信息
dc.ratelimiter.error-msg=请求过于频繁，请稍后再试
```
