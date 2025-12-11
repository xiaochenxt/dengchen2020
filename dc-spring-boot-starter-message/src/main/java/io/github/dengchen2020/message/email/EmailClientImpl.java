package io.github.dengchen2020.message.email;

import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import java.util.Date;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING_ARRAY;

/**
 * 邮件发送
 *
 * @author xiaochen
 * @since 2024/6/4
 */
@NullMarked
public class EmailClientImpl implements EmailClient {

    private static final Logger log = LoggerFactory.getLogger(EmailClientImpl.class);

    private final AsyncTaskExecutor executor;

    static final AsyncTaskExecutor defaultExecutor = new VirtualThreadTaskExecutor("email-");

    private final JavaMailSender javaMailSender;

    private final String username;

    private final String[] to;

    public EmailClientImpl(JavaMailSender javaMailSender) {
        this(javaMailSender, EMPTY_STRING_ARRAY, defaultExecutor);
    }

    public EmailClientImpl(JavaMailSender javaMailSender, String @Nullable... to) {
        this(javaMailSender, to, defaultExecutor);
    }

    public EmailClientImpl(JavaMailSender javaMailSender, String @Nullable[] to, AsyncTaskExecutor executor) {
        this(javaMailSender, null, to, executor);
    }

    public EmailClientImpl(JavaMailSender javaMailSender,@Nullable String username, String @Nullable[] to, AsyncTaskExecutor executor) {
        if (StringUtils.hasText(username)) {
            this.username = username;
        } else {
            if (javaMailSender instanceof JavaMailSenderImpl javaMailSenderImpl) {
                if (!StringUtils.hasText(javaMailSenderImpl.getUsername())) throw new IllegalArgumentException("邮箱邮箱发件人用户名不能为空");
                this.username = javaMailSenderImpl.getUsername();
            }else {
                throw new IllegalArgumentException("邮箱发件人用户名不能为空");
            }
        }
        this.javaMailSender = javaMailSender;
        this.to = to == null ? EMPTY_STRING_ARRAY : to;
        this.executor = executor;
    }

    @Override
    public void sendText(String subject, String text) {
        sendText(subject, text, to);
    }

    @Override
    public void sendText(String subject, String text, String... to) {
        if (to.length == 0) {
            log.error("未指定收件人邮箱，邮件无法发送");
            return;
        }
        executor.execute(() -> sendTextSync(subject, text, to));
    }

    public boolean sendTextSync(String subject, String text, String... to) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(username);
            mail.setTo(to);
            mail.setSentDate(new Date());
            mail.setSubject(subject);
            mail.setText(text);
            javaMailSender.send(mail);
            return true;
        } catch (MailException e) {
            log.error("邮件发送失败，subject：{}，text：{}，to：{}，异常信息：", subject, text, to, e);
            return false;
        }
    }

    @Override
    public void sendMime(String subject, String html) {
        sendMime(subject, html, null, null, to);
    }

    @Override
    public void sendMime(String subject, String html, DataSource @Nullable... attachments) {
        sendMime(subject, html, attachments, null, to);
    }

    @Override
    public void sendMime(String subject, String html, DataSource @Nullable[] attachments, DataSource @Nullable... inlines) {
        sendMime(subject, html, attachments, inlines, to);
    }

    @Override
    public void sendMime(String subject, String html, DataSource @Nullable[] attachments, DataSource @Nullable[] inlines, String... to) {
        if (to.length == 0) {
            log.error("未指定收件人邮箱，mime邮件无法发送");
            return;
        }
        executor.execute(() -> sendMimeSync(subject, html, attachments, inlines, to));
    }

    public boolean sendMimeSync(String subject, String html, DataSource @Nullable[] attachments, DataSource @Nullable[] inlines, String... to) {
        try {
            MimeMessageHelper mime = new MimeMessageHelper(javaMailSender.createMimeMessage(), true, "UTF-8");
            mime.setFrom(username);
            mime.setTo(to);
            mime.setSubject(subject);
            mime.setText(html, true);
            if (attachments != null) {
                for (DataSource attachment : attachments) {
                    if (attachment instanceof FileDataSource fileDataSource) fileDataSource.setFileTypeMap(mime.getFileTypeMap());
                    mime.addAttachment(attachment.getName(), attachment);
                }
            }
            if (inlines != null) {
                for (DataSource inline : inlines) {
                    if (inline instanceof FileDataSource fileDataSource) fileDataSource.setFileTypeMap(mime.getFileTypeMap());
                    mime.addInline(inline.getName(), inline);
                }
            }
            javaMailSender.send(mime.getMimeMessage());
            return true;
        } catch (Exception e) {
            log.error("mime邮件发送失败，subject：{}，html：{}，inlines：{}，attachments：{}，to：{}，异常信息：", subject, html, inlines, attachments, to, e);
            return false;
        }
    }

}
