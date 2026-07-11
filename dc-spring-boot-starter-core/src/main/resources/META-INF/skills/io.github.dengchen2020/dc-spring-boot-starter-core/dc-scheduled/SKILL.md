---
name: dc-scheduled
description: 定时任务，大多数场景使用Spring原生@Scheduled即可，引入Redis后会自动获得多节点防并发能力，仅特殊场景才需要@DcScheduled自定义执行策略。当用户提到定时任务、@Scheduled、@DcScheduled、防并发、分布式定时任务、任务互斥、ScheduledPreventConcurrency、多服务器定时任务等关键词时使用
---

# 定时任务

## 概述

`dc-spring-boot-starter-core` 模块引入 Redis 后，`ScheduledPreventConcurrencyAop` 会自动对 `@Scheduled` 和 `@DcScheduled` 进行拦截，提供多节点执行策略控制。

**大多数场景直接用 `@Scheduled` 即可**，引入 Redis 后默认已有防并发能力。`@DcScheduled` 仅在需要自定义 `concurrency` 或 `seconds` 参数时使用。

| 注解 | 引入 Redis 后的行为 | 推荐场景 |
|------|--------------------|---------|
| `@Scheduled` | 默认独占执行 20 秒窗口 | ✅ 绝大多数场景 |
| `@DcScheduled` | 可自定义 `concurrency` 和 `seconds` | 少数需定制策略的场景 |

## 使用场景

- 定时数据同步（多节点只执行一次）
- 定时清理过期数据
- 定时报表生成
- 需要避免重复执行的定时任务

## 使用示例

### 推荐用法（覆盖绝大多数场景）

```java
@Scheduled(cron = "0 0 2 * * ?")
public void nightlyTask() {
    // 引入 Redis 后，默认 20 秒内其他节点不执行
    // 未引入 Redis 时每个节点独立执行
}

@Scheduled(cron = "0 0/5 * * * ?")
public void periodicTask() {
    // 每5分钟执行，行为同上
}
```

### 自定义执行策略（少数场景使用 `@DcScheduled`）

```java
// 允许并发执行（多节点同时跑）
@DcScheduled(cron = "0 0/5 * * * ?", concurrency = true)
public void concurrentTask() { }

// 延长独占窗口到 60 秒
@DcScheduled(cron = "0 0 2 * * ?", seconds = 60)
public void exclusiveTask() { }
```

## 实现原理

引入 Redis 后，`ScheduledPreventConcurrencyAop` 会对 `@DcScheduled` 和 `@Scheduled` 进行拦截，通过 Redis Key 控制多节点执行策略。

```
@DcScheduled / @Scheduled AOP 拦截
    ↓
ScheduledPreventConcurrencyAop.taskAround()
    ↓
生成 Redis Key：{dc:task}:ClassName:methodName
    ↓ concurrency=true      ↓ concurrency=false
    直接执行                  检查 Redis Key
                               ↓ key 不存在        ↓ key 存在
                          SET key uniqueId seconds   ↓ uniqueId 匹配
                               ↓                     ↓ 否      ↓ 是
                          执行任务              跳过执行     执行任务
```

### 重要行为说明

- `concurrency=false`（默认）：通过 `SET key uniqueId seconds` 写入 Redis，在 `seconds` 秒内其他节点无法再次抢到锁，从而实现独占执行窗口
- `concurrency=true`：跳过 Redis 检查，每个节点独立执行
- 未引入 Redis：AOP 不生效，退化为普通 `@Scheduled`

## 模块结构

```
dc-spring-boot-starter-core/src/main/java/io/github/dengchen2020/core/scheduled/
├── DcScheduled.java                       // 定时任务注解
├── ScheduledPreventConcurrencyAop.java     // 防并发 AOP 实现
└── SchedulingAutoConfiguration.java        // 定时任务自动配置

依赖 Redis 的部分在 redis/ 包中：
└── RedisDependencyAutoConfiguration.java   // 引入 Redis 时自动注册 AOP
```

## 注意事项

1. 多服务器互斥依赖 Redis，未引入 `spring-boot-starter-data-redis` 时退化为普通 `@Scheduled`
2. `seconds` 参数表示 **指定时间内** 其他服务器不可执行，任务执行完成后不会自动释放锁（到期自动释放）
3. 如果任务执行时间超过 `seconds`，其他服务器在锁到期前不会执行，到期后仍可能产生并发，需根据实际执行时长合理设置
4. `@Scheduled` 和 `@DcScheduled` 可混用，都会经过防并发拦截
