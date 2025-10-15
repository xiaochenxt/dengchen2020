package io.github.dengchen2020.message.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 消息配置
 * @author xiaochen
 * @since 2024/6/4
 */
@ConfigurationProperties(prefix = "dc.message")
public class DcMessageBuilder {

    /**
     * 飞书配置
     */
    private FeiShu feiShu = new FeiShu();

    /**
     * 钉钉配置
     */
    private DingTalk dingTalk = new DingTalk();

    /**
     * 邮件配置
     */
    private Email email = new Email();

    /**
     * 微信配置
     */
    private WeChat weChat = new WeChat();

    public FeiShu getFeiShu() {
        return feiShu;
    }

    public void setFeiShu(FeiShu feiShu) {
        this.feiShu = feiShu;
    }

    public DingTalk getDingTalk() {
        return dingTalk;
    }

    public void setDingTalk(DingTalk dingTalk) {
        this.dingTalk = dingTalk;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public WeChat getWeChat() {
        return weChat;
    }

    public void setWeChat(WeChat weChat) {
        this.weChat = weChat;
    }

    public static class FeiShu {

        /**
         * 飞书webhook地址
         */
        private String webhook;

        /**
         * 密钥，用于签名
         */
        private String secret;

        public String getWebhook() {
            return webhook;
        }

        public void setWebhook(String webhook) {
            this.webhook = webhook;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public static class DingTalk {

        /**
         * 钉钉webhook地址
         */
        private String webhook;

        /**
         * 密钥，用于签名
         */
        private String secret;

        public String getWebhook() {
            return webhook;
        }

        public void setWebhook(String webhook) {
            this.webhook = webhook;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public static class Email {

        /**
         * 邮箱收件人
         */
        private String[] to;

        public String[] getTo() {
            return to;
        }

        public void setTo(String[] to) {
            this.to = to;
        }
    }

    public static class WeChat {

        /**
         * 微信webhook地址
         */
        private String webhook;

        public String getWebhook() {
            return webhook;
        }

        public void setWebhook(String webhook) {
            this.webhook = webhook;
        }
    }

}
