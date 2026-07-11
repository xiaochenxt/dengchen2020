---
name: feishu
description: 飞书机器人消息推送，支持文本、富文本消息，支持@指定人。当用户提到飞书消息、飞书机器人、FeiShu、FeiShuClient、飞书通知、发送飞书等关键词时使用
---

# 飞书机器人消息推送

## 概述

`dc-spring-boot-starter-message` 模块的 `FeiShuClient` 提供飞书自定义机器人消息推送能力，支持通过 Webhook 发送文本和富文本消息，可配置签名验证。

| 消息类型 | 类名 | 说明 |
|---------|------|------|
| **文本** | `TextMessage` | 纯文本消息，支持 @指定人或 @所有人 |
| **富文本** | `PostMessage` | 带标题、文本、链接、@的富文本消息，支持中英文双内容 |

## 使用场景

- 系统异常告警通知
- 业务关键操作的通知推送
- 定时任务执行结果汇报
- 运维监控告警

## 使用示例

### 配置

```properties
dc.message.feishu.webhook=https://open.feishu.cn/open-apis/bot/v2/hook/xxx
dc.message.feishu.secret=xxx  # 可选，签名密钥
```

### 文本消息

```java
@Resource
private FeiShuClient feiShuClient;

// 纯文本
feiShuClient.send(new FeiShuClient.TextMessage("你好，世界"));

// @指定人
feiShuClient.send(new FeiShuClient.TextMessage("你好，世界").addAt("user_id_1"));

// @所有人
feiShuClient.send(new FeiShuClient.TextMessage("你好，世界").addAtAll());
```

### 富文本消息

```java
feiShuClient.send(FeiShuClient.PostMessage.builder()
        .zhTitle("中文标题")
        .addText("普通文本", true)                          // unescape=true 不转义
        .addA("链接文字", "https://example.com")            // 超链接
        .addAt("user_id_1")                                // @指定人
        .addAtAll()                                        // @所有人
        .build());
```

### 指定 Webhook 发送

```java
// 发送到指定 webhook（覆盖默认地址）
feiShuClient.send(message, "https://open.feishu.cn/open-apis/bot/v2/hook/xxx");
// 发送到指定 webhook + 签名
feiShuClient.send(message, "https://open.feishu.cn/open-apis/bot/v2/hook/xxx", "xxx");
```

## 实现原理

```
FeiShuClientImpl.send(Message)
    ↓
构建请求体（JSON）+ 时间戳 + 签名
    ↓
HTTP POST → 飞书 Webhook 地址
    ↓
解析响应
```

- 签名算法与钉钉一致：`HmacSHA256(timestamp + "\n" + secret)`，Base64 编码后追加到 URL
- 富文本消息通过 `PostMessage.builder()` 链式构建，支持中英文双内容（`zh_cn` / `en_us`）

## 模块结构

```
dc-spring-boot-starter-message/src/main/java/io/github/dengchen2020/message/
├── feishu/
│   ├── FeiShuClient.java          // 消息接口 + 消息类型定义
│   └── FeiShuClientImpl.java      // 实现
└── config/、properties/
```

## 注意事项

1. 飞书自定义机器人需先在群设置中添加机器人获取 Webhook 地址
2. `TextMessage` 通过 `addAt()` 方法 @指定人，需要在飞书群中获取用户的 `open_id` 或 `user_id`
3. 富文本消息中 `addText()` 的 `un_escape` 参数控制文本是否转义
4. 飞书官方频率限制：每个机器人每分钟最多 20 条消息
