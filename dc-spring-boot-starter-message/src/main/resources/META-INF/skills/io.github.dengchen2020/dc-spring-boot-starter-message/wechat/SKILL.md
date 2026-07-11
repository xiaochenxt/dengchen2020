---
name: wechat
description: 企业微信机器人消息推送，支持文本、Markdown、图片、图文等消息类型，支持@指定人和上传文件。当用户提到企业微信消息、企业微信机器人、WeChat、WeChatClient、企业微信通知、发送企业微信等关键词时使用
---

# 企业微信机器人消息推送

## 概述

`dc-spring-boot-starter-message` 模块的 `WeChatClient` 提供企业微信自定义机器人消息推送能力，支持通过 Webhook 发送多种消息类型。

| 消息类型 | 类名 | 说明 |
|---------|------|------|
| **文本** | `TextMessage` | 纯文本，支持 @指定手机号或 @所有人 |
| **Markdown** | `MarkdownMessage` | Markdown 格式 |
| **图片** | `ImageMessage` | 支持 byte[] 或 Base64+MD5 两种构建方式 |
| **图文** | `NewsMessage` | 标题+描述+图片+跳转链接 |
| **文件** | `FileMessage` | 需先上传获取 media_id |

## 使用场景

- 系统异常告警通知
- 业务关键操作的通知推送
- 定时任务执行结果汇报
- 运维监控告警

## 使用示例

### 配置

```properties
dc.message.we-chat.webhook=https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx
```

### 文本消息

```java
@Resource
private WeChatClient weChatClient;

// 纯文本
weChatClient.send(new WeChatClient.TextMessage("你好，世界"));
// @指定手机号
weChatClient.send(new WeChatClient.TextMessage("你好，世界", "138xxxx0001"));
// @所有人
weChatClient.send(new WeChatClient.TextMessage("你好，世界", true));
```

### Markdown 消息

```java
weChatClient.send(new WeChatClient.MarkdownMessage(
    "# 标题\n**加粗**\n> 引用\n- 列表项"
));
```

### 图片消息

```java
// 通过字节数组构建（自动计算 Base64 和 MD5）
weChatClient.send(new WeChatClient.ImageMessage(imageBytes));

// 通过 Base64 + MD5 构建
weChatClient.send(new WeChatClient.ImageMessage(base64, md5));
```

### 图文消息

```java
weChatClient.send(WeChatClient.NewsMessage.builder()
        .addArticle("标题", "描述", "https://pic-url", "https://link-url")
        .build());
```

### 上传文件

```java
// 上传文件获取 media_id，用于发送文件消息
String mediaId = weChatClient.upload(fileResource, "file", "key");
weChatClient.send(new WeChatClient.FileMessage(mediaId));
```

### 指定 Webhook 发送

```java
weChatClient.send(message, "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx");
```

## 实现原理

```
WeChatClientImpl.send(Message)
    ↓
构建请求体（JSON）
    ↓
HTTP POST → 企业微信 Webhook 地址
    ↓
解析响应
```

- 企业微信无需签名，直接使用 Webhook Key 即可
- `ImageMessage` 的 `byte[]` 构造方法自动计算 Base64 和 MD5

## 模块结构

```
dc-spring-boot-starter-message/src/main/java/io/github/dengchen2020/message/
├── wechat/
│   ├── WeChatClient.java          // 消息接口 + 消息类型定义
│   └── WeChatClientImpl.java      // 实现
└── config/、properties/
```

## 注意事项

1. 企业微信自定义机器人需先在群设置中添加机器人获取 Webhook Key
2. 图片消息的图片大小不能超过 2MB，支持 JPG/PNG 格式
3. 文件上传需通过 `upload()` 方法先获取 `media_id`，再发送文件消息
4. 企业微信官方频率限制：每个机器人每分钟最多 20 条消息
