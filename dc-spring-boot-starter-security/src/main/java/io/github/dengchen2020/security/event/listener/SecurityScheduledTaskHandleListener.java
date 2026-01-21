package io.github.dengchen2020.security.event.listener;

import io.github.dengchen2020.core.event.ScheduledHandleBeforeEvent;
import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 定时任务执行监听器
 *
 * @author xiaochen
 * @since 2024/6/3
 */
public class SecurityScheduledTaskHandleListener {

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    public void handle(ScheduledHandleBeforeEvent event) {
        ProceedingJoinPoint joinPoint = event.joinPoint();
        Class<?> target = joinPoint.getTarget().getClass();
        String key = event.localIpInfo() + ":" + "task:" + target.getSimpleName() + ":" + joinPoint.getSignature().getName();
        if (SecurityContextHolder.getAuthentication() == null) {
            SecurityContextHolder.setAuthentication(new AnonymousAuthentication(key.substring(0, Math.min(key.length(), 64))));
        }
    }

}
