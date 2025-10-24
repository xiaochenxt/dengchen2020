package io.github.dengchen2020.id.snowflake;

import io.github.dengchen2020.id.IdHelper;
import io.github.dengchen2020.id.exception.IdGeneratorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 雪花算法自动配置
 * <p>使用方式：{@link IdHelper#nextId()}</p>
 *
 * @author xiaochen
 * @since 2022/10/16
 */
public class SnowflakeSmartLifecycle implements SmartLifecycle {

    public static final int PHASE = 10240;

    private static final Logger log = LoggerFactory.getLogger(SnowflakeSmartLifecycle.class);

    private volatile boolean running = false;

    private final StringRedisTemplate stringRedisTemplate;

    private final SnowflakeIdGeneratorOptions options;

    public SnowflakeSmartLifecycle(StringRedisTemplate stringRedisTemplate, SnowflakeIdGeneratorOptions options) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.options = options;
    }

    /**
     * 雪花算法机器id集合key
     */
    private static final String SNOWFLAKE_WORKERID_LIST_KEY = "dc:snowflake:workerid:list";

    RedisScript<Long> script = new DefaultRedisScript<>("""
            local workerId = tonumber(ARGV[1])
            local maxWorkerIdNumber = tonumber(ARGV[2])
            local key = KEYS[1]
            for i = workerId, maxWorkerIdNumber do
              if redis.call('SISMEMBER', key, i) == 0 then
                redis.call('SADD', key, i)
                return i
              end
            end
            for i = 0, workerId - 1 do
              if redis.call('SISMEMBER', key, i) == 0 then
                redis.call('SADD', key, i)
                return i
              end
            end
            return -1
            """, Long.class);

    /**
     * 从redis中获取机器id，并设置机器id
     */
    private void setWorkerIdFromRedis(Short workerId, int maxWorkerIdNumber) {
        if (workerId == null || workerId < 0 || workerId > maxWorkerIdNumber) {
            workerId = (short) ThreadLocalRandom.current().nextInt(0, maxWorkerIdNumber);
        }
        Long newWorkerId = stringRedisTemplate.execute(script, List.of(SNOWFLAKE_WORKERID_LIST_KEY), String.valueOf(workerId), String.valueOf(maxWorkerIdNumber));
        if (newWorkerId == -1) throw new IdGeneratorException("workerId已用完，请清理无效的workerId或增加workerIdBitLength");
        options.setWorkerId(newWorkerId.shortValue());
    }

    @Override
    public void start() {
        try {
            //智能设置雪花算法机器id
            setWorkerIdFromRedis(options.getWorkerId(), (1 << options.getWorkerIdBitLength()) - 1);
            running = true;
        } catch (Exception e) {
            running = false;
            throw new IdGeneratorException("雪花算法初始化失败", e);
        }
        IdHelper.setIdGenerator(new SnowflakeIdGenerator(options));
        if (log.isInfoEnabled()) log.info("雪花算法生成器初始化完成，workerId：{}，配置信息：{}", options.getWorkerId(), options);
    }

    /**
     * 从redis中移除机器id
     */
    @Override
    public void stop() {
        stringRedisTemplate.opsForSet().remove(SNOWFLAKE_WORKERID_LIST_KEY, String.valueOf(options.getWorkerId()));
        if (log.isInfoEnabled()) log.info("雪花算法移除workerId：{}", options.getWorkerId());
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return PHASE;
    }
}

