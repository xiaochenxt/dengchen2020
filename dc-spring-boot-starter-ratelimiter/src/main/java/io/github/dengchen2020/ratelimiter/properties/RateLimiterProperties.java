package io.github.dengchen2020.ratelimiter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限流属性配置
 * @author xiaochen
 * @since 2024/7/1
 */
@ConfigurationProperties(prefix = "dc.ratelimiter")
public class RateLimiterProperties {

    /**
     * 是否开启限流
     */
    private boolean enabled = true;

    /**
     * 默认为单机限流
     */
    private Type type = Type.local;

    /**
     * 默认的异常信息
     */
    private String errorMsg = "请求过于频繁，请稍后再试";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        /**
         * 单机限流，默认选项，性能较高
         */
        local,
        /**
         * Redis分布式限流
         */
        redis
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
