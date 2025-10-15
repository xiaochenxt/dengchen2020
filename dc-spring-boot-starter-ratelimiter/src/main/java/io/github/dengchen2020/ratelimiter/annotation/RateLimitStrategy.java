package io.github.dengchen2020.ratelimiter.annotation;

/**
 * 限流策略
 * @author xiaochen
 * @since 2024/7/1
 */
public enum RateLimitStrategy {
    /**
     * 根据用户和uri限流，默认选项，不可用时回退为ipAndUri
     */
    userAndUri,
    /**
     * 根据ip和uri限流
     */
    ipAndUri,
    /**
     * 根据用户限流，不可用时回退为ip
     */
    user,
    /**
     * 根据ip限流
     */
    ip,
    /**
     * 根据uri限流
     */
    uri
}
