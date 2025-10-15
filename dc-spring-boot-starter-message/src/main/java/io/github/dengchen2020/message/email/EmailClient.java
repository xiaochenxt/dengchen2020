package io.github.dengchen2020.message.email;

import org.springframework.mail.SimpleMailMessage;

import java.io.File;

/**
 * 邮件发送接口
 * @author xiaochen
 * @since 2024/6/4
 */
public interface EmailClient {

    /**
     * 发送简单文本邮件
     * @param subject 主题
     * @param text 文本内容
     * @param to 收件人邮箱
     */
    void sendText(String subject, String text, String... to);

    /**
     * 发送mime邮件
     * @param subject 主题
     * @param html html内容
     * @param inlines 内联元素文件
     * @param attachments 附件
     * @param to 收件人邮箱
     */
    void sendMime(String subject, String html, File[] inlines, File[] attachments, String... to);

    /**
     * 发送mime邮件
     * @param subject 主题
     * @param html html内容
     * @param inlines 内联元素文件
     * @param to 收件人邮箱
     */
    void sendMime(String subject, String html, File[] inlines, String... to);

    /**
     * 发送mime邮件
     * @param subject 主题
     * @param html html内容
     * @param attachments 附件
     * @param to 收件人邮箱
     */
    void send(String subject, String html, File[] attachments, String... to);

    /**
     * 发送mime邮件
     * @param subject 主题
     * @param html html内容
     * @param to 收件人邮箱
     */
    void send(String subject, String html, String... to);

}
