消息通知客户端，支持钉钉、飞书、企业微信机器人消息推送及邮件发送。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

钉钉机器人（DcMessageBuilder.DingTalk）：

```java
@Resource
private DingTalkClient dingTalkClient;

// 文本消息
dingTalkClient.send(new DingTalkClient.TextMessage("你好，世界"));
// 文本消息 @指定人
dingTalkClient.send(new DingTalkClient.TextMessage("你好，世界", "138xxxx0001", "138xxxx0002"));
// 文本消息 @所有人
dingTalkClient.send(new DingTalkClient.TextMessage("你好，世界", true));
// 链接消息
dingTalkClient.send(new DingTalkClient.LinkMessage("标题", "内容", "https://example.com"));
// Markdown消息
dingTalkClient.send(new DingTalkClient.MarkdownMessage("标题", "# 一级标题\n## 二级标题"));
// 发送到指定 webhook
dingTalkClient.send(message, "https://oapi.dingtalk.com/robot/send?access_token=xxx");
// 发送到指定 webhook + 签名
dingTalkClient.send(message, "https://oapi.dingtalk.com/robot/send?access_token=xxx", "SEC...");
```

飞书机器人（FeiShuClient）：

```java
@Resource
private FeiShuClient feiShuClient;

// 文本消息
feiShuClient.send(new FeiShuClient.TextMessage("你好，世界")
        .addAt("user_id_1")    // @指定人
        .addAtAll());          // @所有人
// 富文本消息
var zhCn = new FeiShuClient.PostMessage.Post.Zh_cn("标题");
zhCn.addText("文本内容", true);
zhCn.addA("链接", "https://example.com");
zhCn.addAt("user_id_1");
feiShuClient.send(new FeiShuClient.PostMessage(new FeiShuClient.PostMessage.Post(zhCn)));
// 发送到指定 webhook
feiShuClient.send(message, "https://open.feishu.cn/open-apis/bot/v2/hook/xxx");
// 发送到指定 webhook + 签名
feiShuClient.send(message, "https://open.feishu.cn/open-apis/bot/v2/hook/xxx", "xxx");
```

企业微信机器人（WeChatClient）：

```java
@Resource
private WeChatClient weChatClient;

// 文本消息
weChatClient.send(new WeChatClient.TextMessage("你好，世界", "138xxxx0001"));
// 文本消息 @所有人
weChatClient.send(new WeChatClient.TextMessage("你好，世界", true));
// Markdown消息
weChatClient.send(new WeChatClient.MarkdownMessage("# 标题\n**加粗**"));
// 图片消息
weChatClient.send(new WeChatClient.ImageMessage(imageBytes));
// 图文消息
var news = new WeChatClient.NewsMessage("articles", "标题", "https://link-url");
news.setDescription("描述");
news.setPicurl("https://pic-url");
weChatClient.send(news);
```

邮件发送（EmailClient）：

```java
@Resource
private EmailClient emailClient;

// 简单文本邮件
emailClient.sendText("邮件主题", "邮件内容");
// 指定收件人
emailClient.sendText("邮件主题", "邮件内容", "user@example.com");
// HTML邮件
emailClient.sendMime("邮件主题", "<h1>标题</h1><p>内容</p>");
// HTML邮件带附件
emailClient.sendMime("邮件主题", "<h1>标题</h1>", new FileDataSource("/path/file.pdf"));
// 指定收件人（覆盖配置的默认收件人）
emailClient.sendMime("邮件主题", "<h1>标题</h1>", null, "user1@example.com", "user2@example.com");
```

properties 配置：

```properties
# 钉钉
dc.message.ding-talk.webhook=https://oapi.dingtalk.com/robot/send?access_token=xxx
dc.message.ding-talk.secret=SEC...
# 飞书
dc.message.feishu.webhook=https://open.feishu.cn/open-apis/bot/v2/hook/xxx
dc.message.feishu.secret=xxx
# 企业微信
dc.message.we-chat.webhook=https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx
# 邮件默认收件人
dc.message.email.to[0]=admin@example.com
# 邮件配置（Spring Boot标准配置）
spring.mail.host=smtp.example.com
spring.mail.username=noreply@example.com
spring.mail.password=xxx
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```
