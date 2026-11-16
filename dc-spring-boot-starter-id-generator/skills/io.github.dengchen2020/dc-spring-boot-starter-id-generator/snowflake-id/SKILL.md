---
name: snowflake-id
description: 雪花算法ID生成器，支持漂移算法和传统算法，依赖Redis自动分配机器ID。当用户提到雪花算法、全局唯一ID、分布式ID、Snowflake、IdHelper、nextId等关键词时使用
---

# 雪花算法 ID 生成器

## 概述

`dc-spring-boot-starter-id-generator` 模块提供高性能的雪花算法 ID 生成器。当项目引入 `spring-boot-starter-data-redis` 时，机器 ID（WorkerId）会在多节点间自动分配和回收。

| 特性 | 说明 |
|------|------|
| **ID 长度** | 13-16 位之间，默认配置 70 年内不超过 JS 最大值 |
| **计算方法** | 漂移算法（默认）和传统算法两种 |
| **机器 ID** | 引入 Redis 后自动分配，无需手动配置 |
| **编码支持** | 支持 Base62/Base36 编码，缩短 ID 字符串长度 |

## 使用场景

- 数据库主键 ID 生成
- 分布式系统全局唯一标识
- 订单号、流水号生成
- 需要按时间排序的唯一 ID

## 使用示例

引入 Redis 后自动初始化，直接调用 `IdHelper`：

```java
IdHelper.nextId();                        // 生成全局唯一ID（long）
IdHelper.nextIdBase62();                  // 转为62进制字符串（数字+大小写字母）
IdHelper.nextIdBase36Upper();             // 转为36进制大写字符串
IdHelper.nextIdBase36Lower();             // 转为36进制小写字符串
IdHelper.extractTime(id);                 // 从ID中解析出时间戳
IdHelper.newLongFromTimestamp(timestamp);  // 根据时间戳生成ID（同一时间戳ID相同）
```

### 无 Redis 环境手动初始化

```java
SnowflakeIdGeneratorOptions options = new SnowflakeIdGeneratorOptions((short) 1);
options.setMethod((short) 1);
// 更多配置...
IdHelper.setIdGenerator(new SnowflakeIdGenerator(options));
IdHelper.nextId();
```

### Redis 递增 ID 生成器

```java
@Resource
private StringRedisTemplate stringRedisTemplate;

// 全局唯一递增ID
RedisIdGenerator idGenerator = new RedisIdGenerator(stringRedisTemplate, null);
long id = idGenerator.newLong();

// 按业务区分的递增ID
RedisIdGenerator orderIdGen = new RedisIdGenerator(stringRedisTemplate, "order");
long orderId = orderIdGen.newLong();
```

## 实现原理

### 漂移算法 vs 传统算法

| 算法 | mode | 特点 |
|------|------|------|
| **漂移算法** | `1`（默认） | 时间回拨时使用预留序列号，毫秒内序列号耗尽时自动漂移到下一毫秒 |
| **传统算法** | `2` | 严格按时间+序列号生成，序列号耗尽则等待下一毫秒 |

### WorkerId 自动分配

```
应用启动 → SnowflakeSmartLifecycle.start()
    ↓
执行 Redis Lua 脚本
    ↓ 从指定 WorkerId 开始查找                                     ↓
    找到空闲 → SADD 占用并返回                   全部已占用 → 从头开始查找
    ↓                                                     ↓
    设置 WorkerId                                 找到空闲 → 占用          全部已占用 → 抛异常
```

应用停止时自动 `SREM` 释放 WorkerId。

### 时间回拨处理

- 预留序列号 0-4：0 用作手工新值，1-4 用作时间回拨次序
- 最多支持 4 次回拨，超过后从 1 重新计数

## 模块结构

```
dc-spring-boot-starter-id-generator/src/main/java/io/github/dengchen2020/id/
├── IdHelper.java                              // 静态工具入口
├── snowflake/
│   ├── SnowflakeIdGenerator.java              // 雪花算法生成器
│   ├── SnowflakeIdGeneratorOptions.java       // 配置参数
│   ├── SnowWorker.java                        // 漂移算法实现
│   ├── SnowWorker2.java                       // 传统算法实现
│   ├── SnowflakeAutoConfiguration.java        // 自动配置
│   └── SnowflakeSmartLifecycle.java           // 生命周期管理
├── redis/
│   └── RedisIdGenerator.java                  // Redis 递增 ID
└── exception/
    └── IdGeneratorException.java              // ID 生成异常
```

## 注意事项

1. `baseTime`（基础时间）一旦确定后不能再修改，否则生成的 ID 会重复
2. 引入 `spring-boot-starter-data-redis` 后自动启用机器 ID 分配，此时手动调用 `IdHelper.setIdGenerator()` 无效
3. `extractTime()` 解析的是 ID 中编码的时间戳，精度为毫秒
4. `maxSeqNumber` 和 `minSeqNumber` 不宜设置过大，需满足 `seqBitLength` 的位数限制
