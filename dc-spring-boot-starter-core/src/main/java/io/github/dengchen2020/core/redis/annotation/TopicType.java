package io.github.dengchen2020.core.redis.annotation;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;

/**
 * @author xiaochen
 * @since 2024/8/4
 */
public enum TopicType {

    /**
     * 详见：{@link ChannelTopic}
     */
    channel,
    /**
     * 详见：{@link PatternTopic}
     */
    pattern

}
