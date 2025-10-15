package io.github.dengchen2020.message.email;

import jakarta.annotation.Nonnull;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.File;
import java.util.Date;

/**
 * 邮件发送
 *
 * @author xiaochen
 * @since 2024/6/4
 */
public class EmailClientImpl implements EmailClient {

    private static final Logger log = LoggerFactory.getLogger(EmailClientImpl.class);

    private final AsyncTaskExecutor executor;

    private static final AsyncTaskExecutor defaultExecutor = new VirtualThreadTaskExecutor("email-");

    private final JavaMailSender javaMailSender;

    private final String[] to;

    public EmailClientImpl(JavaMailSender javaMailSender, String[] to) {
        this.javaMailSender = javaMailSender;
        this.to = to;
        this.executor = defaultExecutor;
    }

    public EmailClientImpl(JavaMailSender javaMailSender, String[] to, AsyncTaskExecutor executor) {
        this.javaMailSender = javaMailSender;
        this.to = to;
        this.executor = executor;
    }

    @Override
    public void sendText(@Nonnull String subject,@Nonnull String text, String... to) {
        executor.execute(() -> {
            try {
                SimpleMailMessage mail = new SimpleMailMessage();
                if (javaMailSender instanceof JavaMailSenderImpl javaMailSenderImpl) {
                    mail.setFrom(javaMailSenderImpl.getUsername());
                }
                if (to == null || to.length == 0) {
                    mail.setTo(this.to);
                } else {
                    mail.setTo(to);
                }
                mail.setSentDate(new Date());
                mail.setSubject(subject);
                mail.setText(text);
                javaMailSender.send(mail);
            } catch (MailException e) {
                log.error("邮件发送失败，subject：{}，text：{}，to：{}，异常信息：", subject, text, to, e);
            }
        });
    }

    public void sendMime(@Nonnull String subject,@Nonnull String html, File[] inlines, File[] attachments) {
        sendMime(subject, html, inlines, attachments, to);
    }

    public void sendMime(@Nonnull String subject,@Nonnull String html, File[] inlines, File[] attachments, String[] to) {
        executor.execute(() -> {
            try {
                MimeMessageHelper mime = new MimeMessageHelper(javaMailSender.createMimeMessage(), true, "UTF-8");
                if (javaMailSender instanceof JavaMailSenderImpl javaMailSenderImpl) {
                    mime.setFrom(javaMailSenderImpl.getUsername());
                }
                InternetAddress[] address = new InternetAddress[to.length];
                for (int i = 0; i < to.length; i++) {
                    address[i] = new InternetAddress(to[i]);
                }
                mime.setTo(address);
                mime.setSubject(subject);
                mime.setText(html, true);
                if (inlines != null) {
                    for (File inline : inlines) {
                        mime.addInline(inline.getName(), inline);
                    }
                }
                if (attachments != null) {
                    for (File attachment : attachments) {
                        mime.addAttachment(attachment.getName(), attachment);
                    }
                }
                javaMailSender.send(mime.getMimeMessage());
            } catch (Exception e) {
                log.error("mime邮件发送失败，subject：{}，html：{}，inlines：{}，attachments：{}，to：{}，异常信息：", subject, html, inlines, attachments, to, e);
            }
        });
    }

    @Override
    public void sendMime(@Nonnull String subject,@Nonnull String html, File[] inlines, String... to) {
        sendMime(subject, html, inlines, null, to);
    }

    @Override
    public void send(@Nonnull String subject,@Nonnull String html, File[] attachments, String... to) {
        sendMime(subject, html, null, attachments, to);
    }

    @Override
    public void send(@Nonnull String subject,@Nonnull String html, String... to) {
        sendMime(subject, html, null, to);
    }

}
