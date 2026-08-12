---
name: distributed-lock
description: 基于Redisson的分布式锁，支持注解方式和编程方式。当用户提到分布式锁、@Lock、Redisson、DLock、tryLock、锁注解等关键词时使用
---

# 分布式锁

## 概述

`dc-spring-boot-starter-lock` 模块基于 Redisson 提供分布式锁能力，支持声明式注解和编程式 API 两种使用方式。

| 特性 | 注解方式 | 编程方式 |
|------|---------|---------|
| **使用便捷** | `@Lock` 注解，零侵入 | `DLock` 接口，灵活控制 |
| **Key 表达式** | 支持 SpEL 从参数取值 | 手动拼接字符串 |
| **等待超时** | `waitTime` 参数 | 方法参数指定 |
| **自动续期** | `lockTime=-1` 时无限续期 | 同上 |

## 使用场景

- 防止用户重复提交订单
- 秒杀/抢购活动的库存扣减
- 分布式定时任务互斥执行
- 需要互斥访问的共享资源操作

## 使用示例

### 注解方式

```java
@Lock(value = "#id", name = "order", waitTime = 3, lockTime = 10,
      errorMsg = "系统繁忙，请稍后再试")
public void processOrder(Long id) {
    // 业务逻辑
}
```

`@Lock` 参数说明：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| `value` | `""` | 锁的Key，支持 SpEL 表达式（`#p0`、`#id`、`#obj.id`），为空时用方法 `toString()` |
| `name` | `""` | 锁的资源名称，与 value 组合为完整 Key |
| `waitTime` | `-1` | 等待获取锁的时长（秒），-1 表示立即返回 |
| `lockTime` | `-1` | 持有锁时长（秒），-1 表示无限续期直到执行完成 |
| `timeUnit` | `SECONDS` | 时间单位 |
| `errorMsg` | `"请求人数过多，请稍后再试"` | 获取锁失败时的异常提示 |

### 编程方式

```java
@Resource
private DLock dLock;

// 尝试获取锁，获取失败抛异常
dLock.tryLockAndRun("order:" + id, () -> {
    // 业务逻辑
});

// 带等待时间
dLock.tryLockAndRun("order:" + id, 3, TimeUnit.SECONDS, () -> {
    // 业务逻辑
});

// 阻塞等待直到获取锁
dLock.lockAndRun("order:" + id, () -> {
    // 业务逻辑
});

// 带返回值的 Callable 方式
String result = dLock.tryLockAndRun("order:" + id, () -> {
    return callRemoteService();
});

// 自定义锁失败处理
RedissonLock redissonLock = (RedissonLock) dLock;
redissonLock.tryLockAndRun("order:" + id, () -> {
    // 成功执行
}, () -> {
    // 获取锁失败处理
});
```

## 实现原理

```
@Lock 注解
    ↓ AOP 拦截
LockAop.handle()
    ↓
RedissonClient.getLock(key)   →   RLock 实例
    ↓
rLock.tryLock(waitTime, lockTime, unit)
    ↓ 成功                    ↓ 失败
joinPoint.proceed()           throw LockException
    ↓
finally → unlock()
```

- 锁 Key 默认前缀 `dc:lock:`，防止与业务 Key 冲突
- `lockTime=-1` 表示持有锁直到业务执行完成（Watchdog 自动续期）
- 使用 `isHeldByCurrentThread()` 确保只有持有锁的线程才能解锁

## 模块结构

```
dc-spring-boot-starter-lock/src/main/java/io/github/dengchen2020/lock/
├── annotation/Lock.java          // 锁注解
├── api/
│   ├── DLock.java                // 锁接口
│   └── RedissonLock.java         // Redisson 实现
├── config/LockAutoConfiguration.java  // 自动配置
├── LockAop.java                  // AOP 切面实现
└── exception/LockException.java  // 锁异常
```

## 注意事项

1. `@Lock` 与 `@Transactional` 一起使用时，注意 `@Lock` 的 order 需高于事务拦截器，确保在事务内持有锁
2. 锁 Key 的 SpEL 表达式支持：`#p0`(第一个参数)、`#a0`、`#param.id` 等形式
3. Redisson 连接配置默认复用 `spring.data.redis.*`，可通过 `dc.lock.redisson.redis.*` 单独配置
4. `lockTime=-1` 时 Watchdog 每 10 秒续期一次，业务执行时长不建议超过看门狗超时时间
