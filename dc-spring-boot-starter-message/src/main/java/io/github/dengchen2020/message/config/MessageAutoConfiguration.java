package io.github.dengchen2020.message.config;

import io.github.dengchen2020.message.dingtalk.DingTalkClient;
import io.github.dengchen2020.message.dingtalk.DingTalkClientImpl;
import io.github.dengchen2020.message.feishu.FeiShuClient;
import io.github.dengchen2020.message.feishu.FeiShuClientImpl;
import io.github.dengchen2020.message.properties.DcMessageBuilder;
import io.github.dengchen2020.message.wechat.WeChatClient;
import io.github.dengchen2020.message.wechat.WeChatClientImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息发送客户端自动配置
 * @author xiaochen
 * @since 2024/6/4
 */
@EnableConfigurationProperties(DcMessageBuilder.class)
@Configuration(proxyBeanMethods = false)
public final class MessageAutoConfiguration {

    private final DcMessageBuilder.DingTalk dingTalk;

    private final DcMessageBuilder.FeiShu feiShu;

    private final DcMessageBuilder.WeChat weChat;

    MessageAutoConfiguration(DcMessageBuilder dcMessageBuilder) {
        this.dingTalk = dcMessageBuilder.getDingTalk();
        this.feiShu = dcMessageBuilder.getFeiShu();
        this.weChat = dcMessageBuilder.getWeChat();
    }

    @ConditionalOnMissingBean
    @Bean
    FeiShuClient feiShuService(){
        return new FeiShuClientImpl(feiShu.getWebhook(), feiShu.getSecret());
    }

    @ConditionalOnMissingBean
    @Bean
    DingTalkClient dingTalkService(){
        return new DingTalkClientImpl(dingTalk.getWebhook(), dingTalk.getSecret());
    }

    @ConditionalOnMissingBean
    @Bean
    WeChatClient weChatClient(){
        return new WeChatClientImpl(weChat.getWebhook());
    }

}
