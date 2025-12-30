package io.github.dengchen2020.message.config;

import io.github.dengchen2020.message.email.EmailClientImpl;
import io.github.dengchen2020.message.properties.DcMessageBuilder;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 电子邮件发送客户端自动配置
 * @author xiaochen
 * @since 2025/10/24
 */
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
@ConditionalOnBean(JavaMailSender.class)
@ConditionalOnClass(MimeMessage.class)
@Configuration(proxyBeanMethods = false)
public final class EmailClientAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    EmailClientImpl emailService(JavaMailSender javaMailSender, DcMessageBuilder dcMessageBuilder){
        return new EmailClientImpl(javaMailSender, dcMessageBuilder.getEmail().getTo());
    }

}
