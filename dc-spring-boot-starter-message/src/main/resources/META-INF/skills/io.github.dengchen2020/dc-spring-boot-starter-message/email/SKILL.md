---
name: email
description: 邮件发送，支持简单文本邮件和MIME邮件（HTML+附件）。当用户提到邮件发送、EmailClient、发送邮件、邮件通知、邮件附件等关键词时使用
---

# 邮件发送

## 概述

`dc-spring-boot-starter-message` 模块的 `EmailClient` 提供邮件发送能力，支持简单文本邮件和包含 HTML、附件的 MIME 邮件。

| 功能 | 方法 | 说明 |
|------|------|------|
| **文本邮件** | `sendText()` | 纯文本内容 |
| **HTML 邮件** | `sendMime()` | HTML 格式内容 |
| **附件支持** | `sendMime()` + `DataSource` | 支持文件附件 |
| **指定收件人** | 带 `to` 参数的重载 | 覆盖配置的默认收件人 |

## 使用场景

- 系统异常告警邮件通知
- 报表定时发送
- 用户注册/密码找回邮件
- 业务数据导出发送

## 使用示例

### 配置

```properties
# 默认收件人（不指定收件人时使用）
dc.message.email.to[0]=admin@example.com

# Spring Boot 标准邮件配置
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=noreply@example.com
spring.mail.password=xxx
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 发送文本邮件

```java
@Resource
private EmailClient emailClient;

// 发送给默认收件人
emailClient.sendText("邮件主题", "邮件内容");

// 指定收件人
emailClient.sendText("邮件主题", "邮件内容", "user1@example.com", "user2@example.com");
```

### 发送 HTML 邮件

```java
// 发送 HTML 邮件
emailClient.sendMime("邮件主题", "<h1>标题</h1><p>段落内容</p>");
```

### 发送带附件的邮件

```java
import jakarta.activation.FileDataSource;

DataSource attachment = new FileDataSource("/path/file.pdf");
emailClient.sendMime("邮件主题", "<h1>标题</h1>", attachment);
```

### 指定收件人 + 附件

```java
emailClient.sendMime("邮件主题", "<h1>标题</h1>",
    new DataSource[]{new FileDataSource("/path/file.pdf")},
    "user1@example.com", "user2@example.com");
```

## 实现原理

```
EmailClientImpl.sendText(subject, text)
    ↓
JavaMailSender.send(SimpleMailMessage)

EmailClientImpl.sendMime(subject, html, attachments, inlines, to)
    ↓
MimeMessageHelper
    ├── setSubject / setText(html, true)  // HTML 格式
    ├── addAttachment(fileName, DataSource)  // 附件
    ├── addInline(fileName, DataSource)      // 内联元素
    └── setTo(to)  // 未指定时使用配置的默认收件人
    ↓
JavaMailSender.send(MimeMessage)
```

- 邮件发送依赖 `spring-boot-starter-mail`，需引入该依赖并配置邮件服务器
- `sendMime()` 的多个重载方法覆盖了不同参数组合（是否附件、是否内联、是否指定收件人）

## 模块结构

```
dc-spring-boot-starter-message/src/main/java/io/github/dengchen2020/message/
├── email/
│   ├── EmailClient.java          // 邮件发送接口
│   └── EmailClientImpl.java      // 实现
├── config/
│   ├── MessageAutoConfiguration.java
│   └── EmailClientAutoConfiguration.java  // 邮件自动配置
└── properties/
    └── DcMessageBuilder.java     // 配置属性
```

## 注意事项

1. 邮件发送依赖 `spring-boot-starter-mail`，需自行引入该依赖
2. 默认收件人 `dc.message.email.to` 为可选配置，`sendText()`/`sendMime()` 不传收件人时使用默认收件人
3. 附件通过 `DataSource` 接口传递，可使用 `FileDataSource`、`ByteArrayResource` 等实现
4. 内联元素（`inlines`）一般不推荐使用，HTML 中通过 `src` 引用外部资源即可
