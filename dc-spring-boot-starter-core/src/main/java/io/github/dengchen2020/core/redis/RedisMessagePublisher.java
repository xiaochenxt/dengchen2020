package io.github.dengchen2020.core.redis;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.StandardCharsets;

/**
 * Redis消息发布者
 *
 * @author xiaochen
 * @since 2024/6/4
 */
@NullMarked
public class RedisMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisMessagePublisher.class);

    protected final RedisTemplate<byte[], byte[]> redisTemplate;

    private final RedisSerializer<Object> serializer;

    public RedisMessagePublisher(RedisTemplate<byte[], byte[]> redisTemplate, RedisSerializer<Object> serializer) {
        this.redisTemplate = redisTemplate;
        this.serializer = serializer;
    }

    /**
     * 发布消息
     * <p>默认使用Json序列化</p>
     *
     * @param channelName 频道名称
     * @param message     消息
     */
    public void publish(String channelName, Object message) {
        byte[] bytes = serializer.serialize(message);
        convertAndSend(channelName, bytes);
    }

    /**
     * 发布字符串消息
     *
     * @param channelName 频道名称
     * @param message     消息
     */
    public void publish(String channelName, String message) {
        convertAndSend(channelName, message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 发布字节数组消息
     *
     * @param channelName 频道名称
     * @param message     消息
     */
    public void publish(String channelName, byte[] message) {
        convertAndSend(channelName, message);
    }

    protected void convertAndSend(String channelName, byte[] message){
        var count = redisTemplate.convertAndSend(channelName, message);
        if (log.isDebugEnabled()) logMessage(channelName, message, count);
    }

    private void logMessage(String channelName, byte[] message, Long count) {
        log.debug("发布消息 {} 到通道 {}，收到消息的客户端数量：{}", new String(message), channelName, count);
    }

}
