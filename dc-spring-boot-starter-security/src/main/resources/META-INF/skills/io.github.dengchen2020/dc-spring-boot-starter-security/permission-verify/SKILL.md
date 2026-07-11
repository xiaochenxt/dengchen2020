---
name: permission-verify
description: 权限校验，基于@HasPermission注解实现接口级权限控制，认证信息需实现PermissionsInfo接口。当用户提到权限校验、@HasPermission、PermissionVerifier、接口权限、权限控制、权限注解等关键词时使用
---

# 权限校验

## 概述

`dc-spring-boot-starter-security` 模块提供基于 `@HasPermission` 注解的接口权限校验能力。认证信息需实现 `PermissionsInfo` 接口，提供当前用户的权限集合。

| 特性 | 说明 |
|------|------|
| **注解方式** | `@HasPermission` 支持类级别和方法级别 |
| **权限来源** | 认证信息实现 `PermissionsInfo` 接口 |
| **校验逻辑** | 可自行扩展 `PermissionVerifier` 接口 |
| **自动配置** | `dc.security.permission.enabled=true` 开启 |

## 使用场景

- 管理后台的 RBAC 权限控制
- 不同角色访问不同接口
- 细粒度的按钮级权限控制

## 使用示例

### 开启权限校验

```properties
dc.security.permission.enabled=true
```

### 认证信息实现 PermissionsInfo

```java
public record Authentication(String userId, Set<String> permissions)
        implements PermissionsInfo {
    @Override
    public Set<String> permissions() {
        return permissions;
    }
}
```

### 注解使用

```java
// 方法级别：需要同时拥有 admin 和 order:read 权限
@HasPermission({"admin", "order:read"})
@GetMapping("/admin/orders")
public Result listOrders() { ... }

// 类级别：整个控制器都需要权限
@HasPermission("admin")
@RestController
@RequestMapping("/admin")
public class AdminController { ... }
```

### 自定义权限验证器

```java
@Component
public class MyPermissionVerifier implements PermissionVerifier {
    @Override
    public boolean hasPermission(PermissionsInfo permissionsInfo, String[] requirePermissions) {
        // 自定义校验逻辑
        return Arrays.stream(requirePermissions)
                .allMatch(p -> permissionsInfo.permissions().contains(p));
    }
}
```

## 实现原理

```
请求到达
    ↓
PermissionVerifyInterceptor.preHandle()
    ↓
获取 HandlerMethod 上的 @HasPermission 注解
    ├── 无注解 → 放行
    └── 有注解 → 从 SecurityContextHolder 获取当前用户认证信息
                  ↓ 认证信息不是 PermissionsInfo 类型 → 抛异常
                  ↓ 是 PermissionsInfo → 调用 PermissionVerifier.hasPermission()
                      ↓ 有权限 → 放行
                      ↓ 无权限 → throw NoPermissionException
```

## 模块结构

```
dc-spring-boot-starter-security/src/main/java/io/github/dengchen2020/security/
├── annotation/
│   └── HasPermission.java              // 权限校验注解
├── permission/
│   ├── PermissionVerifier.java          // 权限校验接口
│   ├── SimplePermissionVerifier.java    // 默认实现
│   └── PermissionVerifyInterceptor.java // 拦截器
├── config/
│   └── PermissionVerifierAutoConfiguration.java  // 自动配置
└── exception/
    └── NoPermissionException.java
```

## 注意事项

1. 权限校验依赖认证信息，需先配置 Token 认证（JWT 或 Redis 有状态）
2. 认证信息 Record 必须实现 `PermissionsInfo` 接口并返回权限集合
3. 通过 `dc.security.resource.permit-path` 配置的白名单路径会跳过权限校验拦截器
4. `@HasPermission` 的 `value` 为数组时，表示 **同时拥有** 这些权限（AND 关系）
