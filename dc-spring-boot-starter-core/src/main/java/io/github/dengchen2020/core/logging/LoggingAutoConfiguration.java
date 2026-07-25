package io.github.dengchen2020.core.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 日志自动配置
 * @author xiaochen
 * @since 2026/7/25
 */
@ConditionalOnBean(LogbackLoggingSystem.class)
@ConditionalOnProperty(prefix = "dc.logback.async", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(LogbackProperties.class)
@Configuration(proxyBeanMethods = false)
public final class LoggingAutoConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    private final LogbackProperties logbackProperties;

    LoggingAutoConfiguration(LogbackProperties logbackProperties) {
        this.logbackProperties = logbackProperties;
    }

    static final class DcAsyncAppender extends AsyncAppender {
        /**
         * 原有的实现在缓冲区满时会丢弃INFO级别的日志，将其重写为不包含INFO级别的日志
         * @param event
         * @return
         */
        @Override
        protected boolean isDiscardable(ILoggingEvent event) {
            return event.getLevel().toInt() < Level.INFO_INT;
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        var logger = loggerContext.getLogger(LoggingSystem.ROOT_LOGGER_NAME);
        var it = logger.iteratorForAppenders();
        while (it.hasNext()) {
            var appender = it.next();
            if (appender instanceof ConsoleAppender || appender instanceof FileAppender) {
                var asyncAppender = logbackProperties.isDiscardingIncludeInfo() ? new AsyncAppender() : new DcAsyncAppender();
                asyncAppender.setName(appender.getName());
                asyncAppender.setContext(appender.getContext());
                asyncAppender.addAppender(appender);
                asyncAppender.setQueueSize(logbackProperties.getQueueSize());
                asyncAppender.setDiscardingThreshold(logbackProperties.getDiscardingThreshold());
                asyncAppender.setMaxFlushTime((int) logbackProperties.getMaxFlushTime().toMillis());
                asyncAppender.start();
                logger.addAppender(asyncAppender);
                logger.detachAppender(appender);
            }
        }
    }
}
