package io.github.dengchen2020.message.email;

import jakarta.activation.DataSource;
import org.jspecify.annotations.NullMarked;
import org.springframework.lang.Nullable;

/**
 * 邮件发送接口
 * @author xiaochen
 * @since 2024/6/4
 */
@NullMarked
public interface EmailClient {

    /**
     * 发送简单文本邮件
     * @param subject 主题
     * @param text 文本内容
     */
    void sendText(String subject, String text);

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
     */
    void sendMime(String subject, String html);

    /**
     * 发送mime邮件
     * @param subject 主题
     * @param html html内容
     * @param to 收件人邮箱
     */
    default void sendMime(String subject, String html, String... to){
        sendMime(subject,html,null,null,to);
    }

    /**
     * 发送mime邮件
     * @param subject 主题
     * @param html html内容
     * @param attachments 附件
     */
    void sendMime(String subject, String html, @Nullable DataSource... attachments);

    /**
     * 发送mime邮件
     * @param subject 主题
     * @param html html内容
     * @param attachments 附件
     * @param to 收件人邮箱
     */
    default void sendMime(String subject, String html, @Nullable DataSource[] attachments, String... to){
        sendMime(subject,html,attachments,null,to);
    }

    /**
     * 发送mime邮件
     * @param subject 主题
     * @param html html内容
     * @param attachments 附件
     * @param inlines 内联元素文件，一般不用这个也不推荐使用，html中使用src等方式引入即可
     */
    void sendMime(String subject, String html, @Nullable DataSource[] attachments, @Nullable DataSource... inlines);

    /**
     * 发送mime邮件
     * @param subject 主题
     * @param html html内容
     * @param attachments 附件
     * @param inlines 内联元素文件，一般不用这个也不推荐使用，html中使用src等方式引入即可
     * @param to 收件人邮箱
     */
    void sendMime(String subject, String html, @Nullable DataSource[] attachments, @Nullable DataSource[] inlines, String... to);

}
