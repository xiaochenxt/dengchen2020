package io.github.dengchen2020.security.event.listener;

import io.github.dengchen2020.core.event.ScheduledHandleBeforeEvent;
import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import org.springframework.context.event.EventListener;

/**
 * 定时任务执行监听器
 *
 * @author xiaochen
 * @since 2024/6/3
 */
public class SecurityScheduledTaskHandleListener {

    @EventListener
    public void handle(ScheduledHandleBeforeEvent event) {
        var signature = event.signature();
        var key = event.localIpInfo() + ":" + "task:" + signature.getDeclaringType().getSimpleName() + ":" + signature.getName();
        if (SecurityContextHolder.getAuthentication() == null) {
            SecurityContextHolder.setAuthentication(new AnonymousAuthentication(key.substring(0, Math.min(key.length(), 64))));
        }
    }

}
