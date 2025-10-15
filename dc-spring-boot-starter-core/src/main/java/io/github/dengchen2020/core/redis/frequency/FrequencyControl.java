package io.github.dengchen2020.core.redis.frequency;

import java.lang.annotation.*;

/**
 * 频控
 * @author xiaochen
 * @since 2025/5/15
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FrequencyControl {

    /**
     * 每秒请求频次
     */
    int qps() default -1;

    /**
     * 每分钟请求频次
     */
    int qpm() default -1;

    /**
     * 每天请求频次
     */
    int qpd() default -1;

}
