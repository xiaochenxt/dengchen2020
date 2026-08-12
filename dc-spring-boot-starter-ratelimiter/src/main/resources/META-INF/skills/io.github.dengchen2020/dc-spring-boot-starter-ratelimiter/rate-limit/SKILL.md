---
name: rate-limit
description: 请求限流，支持本地限流和Redis分布式限流，注解方式和编程方式。当用户提到限流、@RateLimit、RateLimit、请求频率限制、接口限流、令牌桶、滑动窗口等关键词时使用
---

# 请求限流

## 概述

`dc-spring-boot-starter-ratelimiter` 模块提供请求限流能力。根据项目中是否引入 `spring-boot-starter-data-redis` 自动切换为本地限流或 Redis 分布式限流。

| 模式 | 触发条件 | 算法 |
|------|---------|------|
| **本地限流** | 未引入 Redis | 滑动窗口 + 令牌桶 |
| **分布式限流** | 引入 Redis | Lua 脚本 + INCREX 命令 |

## 使用场景

- 防止接口被恶意高频调用
- 保护后端服务不被突发流量打垮
- 按用户/IP/接口维度进行精细化限流

## 使用示例

### 注解方式

```java
@RateLimit(value = 60, time = 1, timeUnit = TimeUnit.MINUTES, errorMsg = "请求过于频繁")
@GetMapping("/list")
public Result list() {
    return Result.ok();
}
```

`@RateLimit` 支持类级别和方法级别，方法级别覆盖类级别。

### 限流策略

```java
@RateLimit(strategy = RateLimitStrategy.userAndUri) // 按用户+URI限流（默认，用户不可用时回退为ip+uri）
@RateLimit(strategy = RateLimitStrategy.ipAndUri)    // 按IP+URI限流
@RateLimit(strategy = RateLimitStrategy.user)        // 按用户限流
@RateLimit(strategy = RateLimitStrategy.ip)          // 按IP限流
@RateLimit(strategy = RateLimitStrategy.uri)         // 按URI限流
```

### 编程方式 - 本地滑动窗口

```java
LocalRateLimiter limiter = new LocalRateLimiter(Duration.ofSeconds(1));
if (limiter.limit("user:123", 60)) {
    throw new RuntimeException("请求过于频繁");
}
```

### 编程方式 - 本地令牌桶

```java
TokenBucketRateLimiter limiter = new TokenBucketRateLimiter();

// 非阻塞尝试获取令牌
if (limiter.tryAcquire("user:123", 60)) {
    // 通过限流
}

// 阻塞等待获取令牌
limiter.acquire("user:123", 60);

// 带超时的获取令牌
boolean acquired = limiter.tryAcquire("user:123", 60, Duration.ofMillis(500));

// 获取令牌桶状态
TokenBucketRateLimiter.BucketStatus status = limiter.bucketStatus("user:123");
```

### 编程方式 - Redis 分布式限流

```java
@Resource
private StringRedisTemplate stringRedisTemplate;

RedisRateLimiter limiter = new RedisRateLimiter(stringRedisTemplate);
if (limiter.limit("user:123", 60, Duration.ofMinutes(1))) {
    throw new RuntimeException("请求过于频繁");
}
```

## 实现原理

### 模式自动切换

```
项目启动
    ↓
判断 StringRedisTemplate Bean 是否存在
    ↓ 存在                        ↓ 不存在
RedisRateLimiterAutoConfiguration   LocalRateLimiterAutoConfiguration
    ↓                               ↓
Redis 分布式限流（Lua脚本）        本地限流（滑动窗口 + 令牌桶）
```

### 滑动窗口算法（`LocalRateLimiter`）

- 每个 Key 维护一个 `WindowCounter`，记录窗口开始时间和请求计数
- 请求到达时检查是否超出窗口，超出则重置窗口
- 定时清理过期 Key，避免内存泄漏

### 令牌桶算法（`TokenBucketRateLimiter`）

- 每个 Key 维护一个 `TokenBucket`，按固定速率生成令牌
- 支持阻塞式 `acquire()` 和非阻塞式 `tryAcquire()`
- 支持等待超时、动态更新速率、重置令牌桶

### Redis 分布式限流

- Redis 8.8.0+ 使用原生 `INCREX` 命令，性能更优
- 低版本使用 Lua 脚本（GET + INCR + SET EX）保证原子性

## 模块结构

```
dc-spring-boot-starter-ratelimiter/src/main/java/io/github/dengchen2020/ratelimiter/
├── annotation/
│   ├── RateLimit.java              // 限流注解
│   └── RateLimitStrategy.java      // 限流策略枚举
├── local/
│   ├── LocalRateLimiter.java       // 滑动窗口实现
│   ├── TokenBucketRateLimiter.java // 令牌桶实现
│   ├── LocalRateLimiterInterceptor.java
│   └── LocalRateLimiterAutoConfiguration.java
├── redis/
│   ├── RedisRateLimiter.java       // 分布式限流实现
│   ├── RedisRateLimiterInterceptor.java
│   └── RedisRateLimiterAutoConfiguration.java
├── properties/
│   └── RateLimiterProperties.java  // 配置属性
├── AbstractRateLimiterInterceptor.java  // 拦截器基类
└── exception/
    └── RateLimitException.java
```

## 注意事项

1. 本地限流仅支持秒级和分钟级时间窗口，Redis 模式支持任意级别
2. `TokenBucketRateLimiter` 默认最大 Key 数量为 50000，超过时自动清理最早的 Key
3. 注解方式的限流策略默认为 `userAndUri`，要求应用中存在用户认证信息
4. 通过 `dc.ratelimiter.enabled=false` 可全局关闭限流
