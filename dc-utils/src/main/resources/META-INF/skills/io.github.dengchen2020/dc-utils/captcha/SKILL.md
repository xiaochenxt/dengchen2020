---
name: captcha
description: 图片验证码生成工具，支持算术表达式、中文、英文数字组合等验证码类型。当用户提到验证码、CaptchaUtils、图形验证码、算术验证码、中文验证码、登录验证码等关键词时使用
---

# 图片验证码

## 概述

`dc-utils` 模块的 `CaptchaUtils` 提供图片验证码生成能力，基于 easy-captcha 库封装，支持多种验证码类型，输出为 Base64 格式可直接返回前端。

| 类型 | 方法 | 说明 |
|------|------|------|
| **算术** | `arithmetic()` | 数字算术表达式，如 `1+2=?` |
| **中文** | `chinese()` | 中文汉字验证码 |
| **英文数字** | `spec()` | 字母数字组合 |

## 使用场景

- 登录页面验证码
- 注册、找回密码等安全操作验证
- 防止表单重复提交/机器操作

## 使用示例

### 生成算术验证码

```java
ArithmeticCaptcha captcha = CaptchaUtils.arithmetic();

String base64 = captcha.toBase64();  // data:image/png;base64,... 直接返回前端
String code = captcha.text();        // 验证码结果（如 "3"）
```

### 生成中文验证码

```java
ChineseCaptcha captcha = CaptchaUtils.chinese();
String base64 = captcha.toBase64();
String code = captcha.text();
```

### 生成英文数字验证码

```java
SpecCaptcha captcha = CaptchaUtils.spec();
String base64 = captcha.toBase64();
String code = captcha.text();
```

### Controller 示例

```java
@GetMapping("/captcha")
public R<Map<String, String>> captcha() {
    var captcha = CaptchaUtils.arithmetic();
    String uuid = StrUtils.uuidSimplified();
    // 将验证码结果存入 Redis，key = captcha:uuid，value = text
    redisTemplate.opsForValue().set("captcha:" + uuid, captcha.text(),
            1, TimeUnit.MINUTES);
    return R.ok(Map.of("uuid", uuid, "image", captcha.toBase64()));
}
```

## 实现原理

```
CaptchaUtils.arithmetic()
    ↓
ArithmeticCaptcha（easy-captcha）
    ├── 生成算术表达式图片
    └── 计算结果
    ↓
返回 Captcha 对象
    ├── toBase64() → 图片 Base64 字符串
    └── text() → 验证码结果
```

## 模块结构

```
dc-utils/src/main/java/io/github/dengchen2020/core/utils/
└── CaptchaUtils.java       // 验证码工具类
```

## 注意事项

1. 验证码结果需要服务端自行存储验证（推荐存 Redis 并设置过期时间）
2. 前端显示：`<img src="data:image/png;base64,..." />`
3. 依赖 `com.github.whvcse:easy-captcha`，需引入该依赖
