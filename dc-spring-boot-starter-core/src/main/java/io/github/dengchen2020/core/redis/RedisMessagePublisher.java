package io.github.dengchen2020.core.redis;

import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import reactor.util.retry.Retry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Redis消息发布者
 *
 * @author xiaochen
 * @since 2024/6/4
 */
@NullMarked
public class RedisMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisMessagePublisher.class);

    private final ReactiveRedisTemplate<byte[], byte[]> reactiveRedisTemplate;

    private final RedisSerializer<Object> serializer;

    public RedisMessagePublisher(ReactiveRedisTemplate<byte[], byte[]> reactiveRedisTemplate, RedisSerializer<Object> serializer) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
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

    private void convertAndSend(String channelName, byte[] message){
        try {
            reactiveRedisTemplate.convertAndSend(channelName, message).delaySubscription(Duration.ofSeconds(1)).retryWhen(Retry.backoff(3, Duration.ofSeconds(1))).subscribe(count -> {
                if (log.isDebugEnabled()) log.debug("发布消息 {} 到通道 {}，收到消息的客户端数量：{}", new String(message), channelName, count);
            });
        } catch (Exception e) {
            log.error("convertAndSend error", e);
        }
    }

}
