Token 认证与权限校验，支持 JWT 无状态、Redis 有状态两种模式，内置拦截器、过滤器自动配置。

配置认证类型，需在 properties 中指定，三种实现可选：

```properties
dc.security.authentication-type=com.example.Authentication  # 必填，认证信息Record类型全限定名
```

JWT 无状态 Token（不依赖 Redis）：

```properties
dc.security.jwt.secret=your-secret-key  # 密钥，配置后启用JWT模式
dc.security.jwt.expire-in=7200s         # Token有效期（秒）
dc.security.jwt.refresh-expire-in=7d    # 刷新Token有效期
```

```java
@Resource
private JwtTokenService tokenService;

// 创建Token
Authentication authentication = Authentication.create("userId");
TokenInfo tokenInfo = tokenService.createToken(authentication);
// 返回给前端
String token = tokenInfo.token();            // Token值
long expiresIn = tokenInfo.expiresIn();      // 过期时间戳
String refreshToken = tokenInfo.refreshToken(); // 刷新Token

// 读取Token
Authentication auth = tokenService.readToken(token);

// 刷新Token
TokenInfo newToken = tokenService.refreshToken(refreshToken);
```

Redis 有状态 Token — 单设备模式（每个用户仅一个设备在线）：

```properties
dc.security.simple-token.expire-in=7200s   # 有效期，配置后启用单设备模式
dc.security.simple-token.device=web        # 设备标识
dc.security.simple-token.autorenewal=true  # 自动续期
dc.security.simple-token.autorenewal-seconds=1800 # 低于此秒数时续期
```

```java
@Resource
private RedisSimpleTokenService tokenService;

TokenInfo tokenInfo = tokenService.createToken(authentication);
// 同一用户在其他设备登录，旧Token将被顶替并抛出SessionTimeOutException
Authentication auth = tokenService.readToken(token);
tokenService.removeToken(token); // 使Token失效（踢人下线）
```

Redis 有状态 Token — 多设备模式（支持每用户最大在线数量）：

```properties
dc.security.token.expire-in=7200s           # 有效期，配置后启用多设备模式
dc.security.token.max-online-num=3          # 每用户最大在线设备数
dc.security.token.device=web                # 设备标识
dc.security.token.autorenewal=true          # 自动续期
dc.security.token.autorenewal-seconds=1800
```

```java
@Resource
private RedisTokenService tokenService;

TokenInfo tokenInfo = tokenService.createToken(authentication);
// 超出最大在线数，最早登录的设备被踢下线
Authentication auth = tokenService.readToken(token);
tokenService.removeToken(token);  // 踢人下线
long onlineNum = tokenService.onlineNum(token); // 查该用户在线设备数
```

放行无需认证的接口：

```java
@NoTokenRequired         // 类级别，整个控制器无需认证
@GetMapping("/register")
public Result register() { ... }

@GetMapping("/login")
@NoTokenRequired         // 方法级别，该接口无需认证
public Result login() { ... }
```

或在 properties 中配置路径白名单：

```properties
dc.security.resource.permit-path[0]=/api/login
dc.security.resource.permit-path[1]=/api/register
dc.security.resource.permit-path[2]=/api/public/**
```

权限校验（需开启）：

```properties
dc.security.permission.enabled=true
```

认证信息需实现 `PermissionsInfo` 接口：

```java
public record Authentication(String userId, Set<String> permissions)
        implements PermissionsInfo {
    @Override
    public Set<String> permissions() {
        return permissions;
    }
}
```

```java
@HasPermission({"admin", "order:read"})  // 方法级别，需要同时拥有这些权限
@GetMapping("/admin/orders")
public Result listOrders() { ... }
```

密码加密：

```java
@Resource
private BCryptPasswordEncoder passwordEncoder;

// 加密
String encoded = passwordEncoder.encode("rawPassword");
// 校验
boolean matches = passwordEncoder.matches("rawPassword", encoded);
```

properties 配置：

```properties
# 密码加密器配置
dc.security.password-encoder.version=$2a   # BCrypt版本
dc.security.password-encoder.strength=10   # 加密强度
# Token名称（请求头/参数名），默认 Authorization
dc.security.token-name=Authorization
```
