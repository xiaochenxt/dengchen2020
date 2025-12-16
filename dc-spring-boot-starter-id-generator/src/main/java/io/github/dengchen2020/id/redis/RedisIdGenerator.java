package io.github.dengchen2020.id.redis;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

/**
 * 使用redis生成全局唯一id或特定业务的唯一id
 * @author xiaochen
 * @since 2025/12/13
 */
@NullMarked
public class RedisIdGenerator {

    private final StringRedisTemplate redisTemplate;
    private static final String REDIS_ID_GENERATOR_KEY = "dc:id:generator:id";
    private final String redisKey;

    /**
     * 将{@code serviceName} 追加到redisKey中，隔离不同业务的唯一id，为空则不区分业务
     * @param stringRedisTemplate {@link StringRedisTemplate}
     * @param serviceName 业务名称
     */
    public RedisIdGenerator(StringRedisTemplate stringRedisTemplate, String serviceName) {
        this.redisTemplate = stringRedisTemplate;
        this.redisKey = !StringUtils.hasText(serviceName) ? REDIS_ID_GENERATOR_KEY : REDIS_ID_GENERATOR_KEY + ":" + serviceName;
    }

    public long newLong() {
        return redisTemplate.opsForValue().increment(redisKey).longValue();
    }

}
