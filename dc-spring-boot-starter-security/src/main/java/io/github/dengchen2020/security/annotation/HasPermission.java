package io.github.dengchen2020.security.annotation;

import java.lang.annotation.*;

/**
 * 权限校验
 * @author xiaochen
 * @since 2024/7/22
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface HasPermission {

    /**
     * 访问所需的权限
     */
    String[] value() default {};

}
