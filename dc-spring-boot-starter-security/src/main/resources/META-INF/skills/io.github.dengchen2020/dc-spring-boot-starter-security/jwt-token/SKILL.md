---
name: jwt-token
description: 基于JWT的无状态Token认证服务，提供创建Token、读取Token、刷新Token能力。当用户提到JWT、无状态Token、JwtTokenService、Token认证、无状态登录、refreshToken等关键词时使用
---

# JWT 无状态 Token 认证

## 概述

`dc-spring-boot-starter-security` 模块的 `JwtTokenService` 提供基于 JWT 的无状态 Token 认证。不依赖 Redis 等第三方组件，适合对 Token 控制力要求不高、追求简单稳定的场景。

| 特性 | JWT 无状态 | Redis 有状态 |
|------|-----------|-------------|
| **依赖** | 不依赖外部组件 | 依赖 Redis |
| **控制力** | 弱，无法实时踢人/封禁 | 强，可踢人下线 |
| **Token 刷新** | 需自行实现刷新逻辑 | 内置刷新机制 |
| **推荐有效期** | 短（建议 2 小时内） | 可长可短 |

## 使用场景

- 无需 Redis 的轻量应用
- Token 有效期短、安全要求不高的场景
- 无状态微服务之间的认证
- 与有状态 Token 结合实现双 Token 机制

## 使用示例

### 配置

```properties
dc.security.authentication-type=com.example.Authentication  # 认证信息Record类型
dc.security.jwt.secret=your-secret-key                       # JWT密钥（配置后启用JWT模式）
dc.security.jwt.expire-in=7200s                              # Token有效期,默认7200秒
dc.security.jwt.refresh-expire-in=7d                         # 刷新Token有效期
```

### 创建和验证 Token

```java
@Resource
private JwtTokenService tokenService;

// 创建Token
Authentication authentication = Authentication.create("userId");
TokenInfo tokenInfo = tokenService.createToken(authentication);
// 返回给前端
String token = tokenInfo.token();              // Token值
long expiresIn = tokenInfo.expiresIn();        // 过期时间戳
String refreshToken = tokenInfo.refreshToken();// 刷新Token（配置refreshExpireIn后才有）

// 读取Token（每次请求时调用）
Authentication auth = tokenService.readToken(token);
String userId = auth.userId();

// 刷新Token
TokenInfo newToken = tokenService.refreshToken(refreshToken);
```

### 自定义认证信息类型

```java
// 定义认证信息 Record
public record AppAuthentication(String userId, String nickname, String role)
        implements Authentication {
}
```

### 配置放行接口

```java
@GetMapping("/login")
@NoTokenRequired   // 无需Token认证
public Result login() { ... }
```

或在 properties 中全局配置：

```properties
dc.security.resource.permit-path[0]=/api/login
dc.security.resource.permit-path[1]=/api/public/**
```

## 实现原理

### Token 创建

```
createToken(authentication)
    ↓
JWT.create()
    ├── sub = userId
    ├── jti = uuid
    ├── exp = System.currentTimeMillis() + expireSeconds
    ├── payload = authentication (JSON 序列化)
    └── sign with HMAC-SHA256
    ↓
返回 TokenInfo { token, expiresIn, refreshToken, refreshTokenExpiresIn }
```

### Token 验证

```
readToken(token)
    ↓
JWT.decode(token)
    ↓ 解析成功            ↓ 过期/签名无效
反序列化 payload           ↓
    ↓                      ↓ 有 refreshToken
返回 Authentication    尝试 refreshToken
                        ↓ 成功           ↓ 失败
                       返回新 Token    throw SessionTimeOutException
```

### 刷 Token

```
refreshToken(refreshToken)
    ↓
解码 refreshToken → 提取 jti, sub
    ↓
调用 checkRefreshToken(jti, sub) → 默认 throw（需子类覆盖实现存储验证）
    ↓
createToken(authentication) → 返回新 TokenInfo
```

## 模块结构

```
dc-spring-boot-starter-security/src/main/java/io/github/dengchen2020/security/
├── authentication/token/
│   ├── TokenService.java                  // Token 认证接口
│   ├── JwtTokenService.java               // JWT 无状态实现
│   ├── RedisSimpleTokenService.java       // Redis 有状态(单设备)
│   ├── RedisTokenService.java             // Redis 有状态(多设备)
│   ├── AbstractStateTokenService.java     // 有状态基类
│   ├── JwtHelper.java                     // JWT 编解码工具
│   └── TokenConstant.java                 // 常量
├── annotation/
│   ├── HasPermission.java                 // 权限校验注解
│   └── NoTokenRequired.java               // 免认证注解
├── config/
│   ├── TokenAutoConfiguration.java        // Token 自动配置
│   ├── SecurityAutoConfiguration.java     // 安全自动配置
│   └── PermissionVerifierAutoConfiguration.java
├── properties/
│   └── SecurityProperties.java            // 安全配置属性
└── exception/
    ├── SessionTimeOutException.java
    └── NoPermissionException.java
```

## 注意事项

1. JWT 密钥 `dc.security.jwt.secret` 必须妥善保管，泄漏后任何人都可以签发有效 Token
2. JWT 无状态 Token 无法主动失效（无法踢人），建议 Token 有效期不要设置过长
3. `refreshToken` 的存储验证（`checkRefreshToken` 方法）需要子类覆盖实现，否则默认抛异常
4. `dc.security.authentication-type` 必须配置为实现了 `Authentication` 接口的 Record 类型全限定名
