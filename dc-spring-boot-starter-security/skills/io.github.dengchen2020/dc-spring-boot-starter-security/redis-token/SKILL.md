---
name: redis-token
description: 基于Redis的有状态Token认证，支持单设备模式和每用户最大在线数控制，可踢人下线。当用户提到Redis有状态Token、RedisSimpleTokenService、RedisTokenService、踢人下线、Token在线数控制、Session管理等关键词时使用
---

# Redis 有状态 Token 认证

## 概述

`dc-spring-boot-starter-security` 模块提供基于 Redis 的有状态 Token 认证。相比 JWT 无状态方式，有状态 Token 可以对登录会话进行精细控制，支持踢人下线、多设备管理等能力。

| 特性 | `RedisSimpleTokenService` | `RedisTokenService` |
|------|--------------------------|-------------------|
| **每用户设备数** | 仅一个设备在线 | 可配置最大在线数 |
| **踢人下线** | 新登录顶替旧设备 | 超出最大数时踢掉最早设备 |
| **设备标识** | 支持按设备类型隔离 | 支持按设备类型隔离 |
| **Token 续期** | ✅ 自动续期 | ✅ 自动续期 |

## 使用场景

- 需要踢人下线的管理后台
- 限制用户多设备登录
- 需要实时控制登录会话的应用
- 对安全性要求较高的系统

## 使用示例

### 单设备模式（每个用户仅一个设备在线）

```properties
dc.security.authentication-type=com.example.Authentication
dc.security.simple-token.expire-in=7200s
dc.security.simple-token.device=web
dc.security.simple-token.autorenewal=true
dc.security.simple-token.autorenewal-seconds=1800
```

```java
@Resource
private RedisSimpleTokenService tokenService;

// 创建Token（同一用户在其他设备登录时，旧Token被顶替）
TokenInfo tokenInfo = tokenService.createToken(authentication);

// 读取Token（被顶替时会抛出 SessionTimeOutException）
Authentication auth = tokenService.readToken(token);

// 踢人下线
tokenService.removeToken(token);
```

### 多设备模式（控制每用户最大在线数）

```properties
dc.security.authentication-type=com.example.Authentication
dc.security.token.expire-in=7200s
dc.security.token.max-online-num=3
dc.security.token.device=web
dc.security.token.autorenewal=true
dc.security.token.autorenewal-seconds=1800
```

```java
@Resource
private RedisTokenService tokenService;

// 创建Token（超出最大在线数时，最早登录的设备被踢下线）
TokenInfo tokenInfo = tokenService.createToken(authentication);

// 读取Token
Authentication auth = tokenService.readToken(token);

// 踢人下线
tokenService.removeToken(token);

// 查询该用户的在线设备数
long onlineNum = tokenService.onlineNum(token);
```

## 实现原理

### 单设备模式

```
createToken(authentication)
    ↓
生成 token + 序列化 authentication
    ↓
Redis pipeline:
    SET userId → token (expire)
    SET userId:info → payload (expire)
    ↓
readToken(token)
    ↓ GET userId
    ↓ token 匹配 → 返回认证信息
    ↓ 不匹配 → throw SessionTimeOutException("已在其他设备登录")
```

### 多设备模式

```
createToken(authentication)
    ↓
生成 token + 序列化 authentication
    ↓
Redis pipeline:
    RPUSH userId:list → token   (expire)
    SET userId:info → payload   (expire)
    LTRIM userId:list → -maxOnlineNum, -1
    ↓
readToken(token)
    ↓ LINDEX userId:list 查找 token
    ↓ 存在 → 返回认证信息
    ↓ 不存在 → null（需要重新登录）
```

### 自动续期

当 `autorenewal=true` 时，每次 `readToken()` 检查剩余 TTL，低于 `autorenewalSeconds` 时自动延长过期时间。

## 模块结构

```
dc-spring-boot-starter-security/src/main/java/io/github/dengchen2020/security/
├── authentication/token/
│   ├── TokenService.java                  // Token 认证接口
│   ├── RedisSimpleTokenService.java       // 单设备模式
│   ├── RedisTokenService.java             // 多设备模式
│   ├── AbstractStateTokenService.java     // 有状态基类
│   └── ...
└── ...
```

## 注意事项

1. 通过配置 `dc.security.simple-token.expire-in` 或 `dc.security.token.expire-in` 来启用对应的有状态模式，两者互斥
2. 有状态 Token 依赖 Redis，需引入 `spring-boot-starter-data-redis`
3. `tokenService.removeToken(token)` 用于踢人下线，调用后该 Token 立即失效
4. `device` 参数可用于按设备类型隔离登录（如 `web`、`mobile`、`pc`），不同设备类型互不影响
