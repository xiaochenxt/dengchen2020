package io.github.dengchen2020.core.redis.annotation;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.data.redis.listener.ChannelTopic;

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
public @interface RedisMessageListener {

    /**
     * 主题名称
     */
    String value();

    /**
     * redis订阅通道类型，默认{@link ChannelTopic}
     */
    TopicType type() default TopicType.channel;

}
