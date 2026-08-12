package io.github.dengchen2020.core.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * SpringBoot版本匹配
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnSpringBootVersionCondition.class)
public @interface ConditionalOnSpringBootVersion {
    /**
     * 版本号，当SpringBoot版本号大于等于该版本时条件成立
     */
    String equalOrNewer() default "";

    /**
     * 当前SpringBoot版本号小于该版本时条件成立
     */
    String olderThan() default "";

}
