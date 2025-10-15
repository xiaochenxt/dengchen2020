package io.github.dengchen2020.security.annotation;

import java.lang.annotation.*;

/**
 * 为属性配置无需Token认证提供注解方式，标识接口无需Token认证
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NoTokenRequired {

    /**
     * 截止时间（yyyy-MM-dd HH:mm:ss），到期后需要Token认证
     */
    String deadlineString() default "";

}
