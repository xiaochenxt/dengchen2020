package io.github.dengchen2020.core.scheduled;

import io.github.dengchen2020.core.event.ScheduledHandleBeforeEvent;
import io.github.dengchen2020.core.utils.IPUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 定时任务多台服务器并发处理
 *
 * @author xiaochen
 * @since 2022/4/1 11:18
 */
@Aspect
public class ScheduledConcurrencyAop {

    public ScheduledConcurrencyAop(StringRedisTemplate stringRedisTemplate, Environment environment, ApplicationEventPublisher eventPublisher) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.environment = environment;
        this.eventPublisher = eventPublisher;
    }

    private final StringRedisTemplate stringRedisTemplate;

    private final Environment environment;

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
     * @return
     * @throws Throwable
     */
    private Object handle(ProceedingJoinPoint joinPoint, boolean concurrency, long seconds) throws Throwable {
        Class<?> target = joinPoint.getTarget().getClass();
        String key = "task:" + target.getSimpleName() + ":" + joinPoint.getSignature().getName();
        String port = environment.getProperty("server.port");
        String localIpInfo = StringUtils.hasText(port) ? IPUtils.getLocalAddr() + ":" + port : IPUtils.getLocalAddr();
        eventPublisher.publishEvent(new ScheduledHandleBeforeEvent(joinPoint, localIpInfo));
        if (concurrency) {
            return joinPoint.proceed();
        }
        //阻止指定时间内的重复执行
        if (Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key, localIpInfo, Duration.ofSeconds(seconds)))) {
            return joinPoint.proceed();
        }
        return null;
    }

}
