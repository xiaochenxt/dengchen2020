package io.github.dengchen2020.core.logging;

import ch.qos.logback.core.AsyncAppenderBase;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Logback配置属性
 * @author xiaochen
 * @since 2026/7/25
 */
@ConfigurationProperties("dc.logback.async")
public class LogbackProperties {

    /**
     * 是否启用异步日志
     */
    private boolean enabled = false;

    /**
     * 异步日志队列大小
     */
    private int queueSize = 4096;

    /**
     * 应用关闭时允许等待该时长处理队列中的残留日志，如果超过这个时间将丢弃剩余的日志，默认1秒
     */
    private Duration maxFlushTime = Duration.ofMillis(AsyncAppenderBase.DEFAULT_MAX_FLUSH_TIME);

    /**
     * 异步日志在缓冲区满时的丢弃阈值，当队列空位小于该阈值时将丢弃TRACE、DEBUG级别的日志。相关配置：{@link discardingIncludeInfo}
     */
    private int discardingThreshold = -1;

    /**
     * 异步日志在缓冲区满时默认是丢弃TRACE、DEBUG级别的日志，为true时将包含INFO级别
     */
    private boolean discardingIncludeInfo = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public Duration getMaxFlushTime() {
        return maxFlushTime;
    }

    public void setMaxFlushTime(Duration maxFlushTime) {
        this.maxFlushTime = maxFlushTime;
    }

    public int getDiscardingThreshold() {
        return discardingThreshold;
    }

    public void setDiscardingThreshold(int discardingThreshold) {
        this.discardingThreshold = discardingThreshold;
    }

    public boolean isDiscardingIncludeInfo() {
        return discardingIncludeInfo;
    }

    public void setDiscardingIncludeInfo(boolean discardingIncludeInfo) {
        this.discardingIncludeInfo = discardingIncludeInfo;
    }

}
