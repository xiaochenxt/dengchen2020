package io.github.dengchen2020.security.annotation;

import java.lang.annotation.*;

/**
 * 标识接口无需Token认证
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface NoTokenRequired {

}
