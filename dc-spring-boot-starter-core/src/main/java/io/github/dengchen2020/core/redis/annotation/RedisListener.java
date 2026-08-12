package io.github.dengchen2020.core.redis.annotation;

import org.springframework.aot.hint.annotation.Reflective;

import java.lang.annotation.*;

/**
 * redis消息订阅监听注册
 * @author xiaochen
 * @since 2024/8/3
 */
@Reflective
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisListener {

    /**
     * 主题名称
     */
    String value();

}
