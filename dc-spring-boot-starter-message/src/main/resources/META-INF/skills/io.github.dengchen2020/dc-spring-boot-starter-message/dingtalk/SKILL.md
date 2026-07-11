---
name: dingtalk
description: 钉钉机器人消息推送，支持文本、链接、Markdown等消息类型，支持@指定人和签名验证。当用户提到钉钉消息、钉钉机器人、DingTalk、钉钉通知、发送钉钉等关键词时使用
---

# 钉钉机器人消息推送

## 概述

`dc-spring-boot-starter-message` 模块的 `DingTalkClient` 提供钉钉自定义机器人消息推送能力，支持通过 Webhook 发送多种消息类型，可配置签名验证。

| 消息类型 | 类名 | 说明 |
|---------|------|------|
| **文本** | `TextMessage` | 纯文本消息，支持 @指定人或@所有人 |
| **链接** | `LinkMessage` | 带标题、描述和跳转链接的消息 |
| **Markdown** | `MarkdownMessage` | Markdown 格式富文本消息 |

## 使用场景

- 系统异常告警通知
- 业务关键操作的通知推送
- 定时任务执行结果汇报
- 运维监控告警

## 使用示例

### 配置

```properties
dc.message.ding-talk.webhook=https://oapi.dingtalk.com/robot/send?access_token=xxx
dc.message.ding-talk.secret=SEC...  # 可选，签名密钥
```

### 文本消息

```java
@Resource
private DingTalkClient dingTalkClient;

// 纯文本
dingTalkClient.send(new DingTalkClient.TextMessage("你好，世界"));

// @指定人
dingTalkClient.send(new DingTalkClient.TextMessage("你好，世界", "138xxxx0001", "138xxxx0002"));

// @所有人
dingTalkClient.send(new DingTalkClient.TextMessage("你好，世界", true));
```

### 链接消息

```java
dingTalkClient.send(new DingTalkClient.LinkMessage(
    "标题",                    // 消息标题
    "内容描述",                // 消息内容
    "https://example.com"     // 点击跳转URL
));
```

### Markdown 消息

```java
dingTalkClient.send(new DingTalkClient.MarkdownMessage(
    "标题",                    // 标题
    "# 一级标题\n## 二级标题\n**加粗**\n> 引用"  // Markdown内容
));
```

### 指定 Webhook 发送

```java
// 发送到指定 webhook（覆盖配置的默认地址）
dingTalkClient.send(message, "https://oapi.dingtalk.com/robot/send?access_token=xxx");

// 发送到指定 webhook + 签名
dingTalkClient.send(message, "https://oapi.dingtalk.com/robot/send?access_token=xxx", "SEC...");
```

## 实现原理

```
DingTalkClientImpl.send(Message)
    ↓
构建请求体（JSON）+ 时间戳 + 签名
    ↓
HTTP POST → 钉钉 Webhook 地址
    ↓
解析响应
```

- 签名算法：`HmacSHA256(timestamp + "\n" + secret)`，Base64 编码后追加到请求 URL
- 所有请求使用虚拟线程执行，不阻塞业务线程

## 模块结构

```
dc-spring-boot-starter-message/src/main/java/io/github/dengchen2020/message/
├── dingtalk/
│   ├── DingTalkClient.java          // 消息接口 + 消息类型定义
│   └── DingTalkClientImpl.java      // 实现
├── config/
│   └── MessageAutoConfiguration.java  // 自动配置
├── properties/
│   └── DcMessageBuilder.java         // 配置属性
├── email/
├── feishu/
└── wechat/
```

## 注意事项

1. 钉钉自定义机器人需先在群设置中添加机器人获取 Webhook 地址
2. 开启签名验证后，需要在钉钉机器人安全设置中配置相同的签名密钥
3. 钉钉官方消息频率限制：每个机器人每分钟最多发送 20 条消息
4. 消息内容长度限制请参考钉钉官方文档，Markdown 消息不超过 5000 字符
