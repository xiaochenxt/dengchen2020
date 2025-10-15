package io.github.dengchen2020.ratelimiter.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解
 *
 * @author xiaochen
 * @since 2024/4/18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RateLimit {

    /**
     * 限制次数
     */
    int value() default 60;

    /**
     * 时间单位，仅支持秒级、分钟级
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * 异常提示
     */
    String errorMsg() default "";

    /**
     * 限流策略
     */
    RateLimitStrategy strategy() default RateLimitStrategy.userAndUri;

}

