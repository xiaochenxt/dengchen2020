package io.github.dengchen2020.ratelimiter.redis;

import io.github.dengchen2020.core.utils.VersionUtils;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.output.IntegerListOutput;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

/**
 * 基于 lettuce 原生命令 INCREX 实现的分布式限流，
 * 单条命令即完成自增、上限判断和过期时间设置，
 * 仅当连接工厂为 lettuce 且 Redis 版本不低于 8.8.0 时使用，其余情况使用父类 {@link RedisRateLimiter} 的 lua 脚本实现
 * @author xiaochen
 * @since 2026/11/16
 */
public class LettuceRateLimiter extends RedisRateLimiter {

    /**
     * INCREX 命令自 Redis 8.8.0 起提供
     */
    private static final String INCREX_SINCE_VERSION = "8.8.0";

    public LettuceRateLimiter(StringRedisTemplate redisTemplate) {
        super(redisTemplate);
    }

    /**
     * 是否可以使用本实现：连接工厂为 lettuce 且 Redis 版本不低于 8.8.0
     *
     * @param redisTemplate {@link StringRedisTemplate}
     * @return true：使用本实现 false：使用父类的 lua 脚本实现
     */
    public static boolean support(StringRedisTemplate redisTemplate) {
        if (!(redisTemplate.getRequiredConnectionFactory() instanceof LettuceConnectionFactory)) return false;
        var version = redisTemplate.execute((RedisCallback<String>) connection -> {
            Properties info = connection.serverCommands().info();
            return info == null ? null : (String) info.get("redis_version");
        });
        return version != null && VersionUtils.compareVersion(version, INCREX_SINCE_VERSION) >= 0;
    }

    /**
     * 是否被限制
     *
     * @param limitKey 限制标识符
     * @param limitNum 限制的次数
     * @param duration 时间窗口
     * @return true：被限制 false：未被限制
     */
    @Override
    public boolean limit(String limitKey, int limitNum, Duration duration) {
        var key = RATE_LIMIT_PREFIX + limitKey;
        var result = redisTemplate.execute((RedisCallback<List<Long>>) connection -> LettuceIncrex.execute(connection, key, limitNum, duration.toSeconds()));
        // INCREX 返回[自增后的值, 实际自增量]，实际自增量为 0 说明已达到上限，本次请求被限制
        return result.get(1) == 0;
    }

    /**
     * Redis 8.8.0 新增命令 INCREX 的 lettuce 实现，
     * 单独抽取内部类，保证未引入 lettuce 时不会加载 lettuce 相关类
     */
    private static final class LettuceIncrex {

        private static final String INCREX = "INCREX";
        private static final byte[] BYINT = "BYINT".getBytes(StandardCharsets.UTF_8);
        private static final byte[] ONE = "1".getBytes(StandardCharsets.UTF_8);
        private static final byte[] UBOUND = "UBOUND".getBytes(StandardCharsets.UTF_8);
        private static final byte[] EX = "EX".getBytes(StandardCharsets.UTF_8);
        private static final byte[] ENX = "ENX".getBytes(StandardCharsets.UTF_8);

        private LettuceIncrex() {
        }

        /**
         * 执行 INCREX key BYINT 1 UBOUND limitNum EX exSeconds ENX
         *
         * @param connection {@link LettuceConnection}
         * @param key        限制键
         * @param limitNum   限制的次数
         * @param exSeconds  时间窗口（秒）
         * @return 两个元素：[自增后的值, 实际自增量]
         */
        static List<Long> execute(RedisConnection connection, String key, long limitNum, long exSeconds) {
            var lettuceConnection = (LettuceConnection) connection.commands();
            @SuppressWarnings("unchecked")
            var result = (List<Long>) lettuceConnection.execute(INCREX,
                    new IntegerListOutput<>(ByteArrayCodec.INSTANCE),
                    key.getBytes(StandardCharsets.UTF_8),
                    BYINT, ONE,
                    UBOUND, String.valueOf(limitNum).getBytes(StandardCharsets.UTF_8),
                    EX, String.valueOf(exSeconds).getBytes(StandardCharsets.UTF_8),
                    ENX);
            return result;
        }
    }
}
