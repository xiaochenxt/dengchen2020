package io.github.dengchen2020.core.scheduled;

import io.github.dengchen2020.core.event.ScheduledHandleBeforeEvent;
import io.github.dengchen2020.core.utils.IPUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务多台服务器并发处理
 *
 * @author xiaochen
 * @since 2022/4/1 11:18
 */
@Aspect
public class ScheduledConcurrencyAop implements SmartLifecycle {

    public static final int PHASE = 20000;

    private volatile boolean running = false;

    /**
     * 服务器实例唯一id
     */
    private final String uniqueId;
    private final ConcurrentHashMap.KeySetView<String, Boolean> keys = ConcurrentHashMap.newKeySet();

    public ScheduledConcurrencyAop(StringRedisTemplate stringRedisTemplate, Environment environment, ApplicationEventPublisher eventPublisher) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.eventPublisher = eventPublisher;
        this.uniqueId = UUID.randomUUID().toString().replace("-", "");
        this.port = environment.getProperty("server.port");
    }

    private final StringRedisTemplate stringRedisTemplate;

    private final String port;

    private final ApplicationEventPublisher eventPublisher;

    @Around(value = "@annotation(scheduled)")
    public Object taskAround(ProceedingJoinPoint joinPoint, Scheduled scheduled) throws Throwable {
        return handle(joinPoint, false, 20);
    }

    @Around(value = "@annotation(scheduled)")
    public Object taskAround(ProceedingJoinPoint joinPoint, DcScheduled scheduled) throws Throwable {
        return handle(joinPoint, scheduled.concurrency(), scheduled.seconds());
    }

    /**
     * 对定时任务做前置处理
     *
     * @param joinPoint
     * @param concurrency 允许多台服务器同时执行（默认false）
     * @param seconds 指定时间内不允许其他服务器执行
     * @return
     * @throws Throwable
     */
    private Object handle(ProceedingJoinPoint joinPoint, boolean concurrency, long seconds) throws Throwable {
        Class<?> target = joinPoint.getTarget().getClass();
        String key = "dc:task:" + target.getSimpleName() + ":" + joinPoint.getSignature().getName();
        keys.add(key);
        String localIpInfo = StringUtils.hasText(port) ? IPUtils.getLocalAddr() + ":" + port : IPUtils.getLocalAddr();
        eventPublisher.publishEvent(new ScheduledHandleBeforeEvent(joinPoint, localIpInfo));
        if (concurrency) return joinPoint.proceed();
        var str = stringRedisTemplate.opsForValue().get(key);
        if (str != null) return uniqueId.equals(str) ? joinPoint.proceed() : null;
        //获得指定时间内的执行权
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, uniqueId, seconds, TimeUnit.SECONDS))) {
            return joinPoint.proceed();
        }
        return null;
    }

    @Override
    public void start() {
        running = true;
    }

    private static final RedisScript<Void> stopScript = new DefaultRedisScript<>("""
            local uniqueId = ARGV[1]
            for _, key in ipairs(KEYS) do
                if redis.call('get', key) == uniqueId then
                    redis.call('del', key)
                end
            end
            """, Void.class);

    @Override
    public void stop() {
        if(!keys.isEmpty()) stringRedisTemplate.execute(stopScript, new ArrayList<>(keys), uniqueId);
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
