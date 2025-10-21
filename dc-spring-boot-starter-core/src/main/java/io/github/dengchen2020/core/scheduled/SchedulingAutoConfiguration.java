package io.github.dengchen2020.core.scheduled;

import jakarta.annotation.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 定时任务自动配置
 *
 * @author xiaochen
 * @since 2024/5/31
 */
@ConditionalOnProperty(value = "dc.task.scheduling.optimize", matchIfMissing = true, havingValue = "true")
@Configuration(proxyBeanMethods = false)
public final class SchedulingAutoConfiguration implements SchedulingConfigurer {

    private final ThreadPoolTaskSchedulerBuilder threadPoolTaskSchedulerBuilder;

    SchedulingAutoConfiguration(@Nullable ThreadPoolTaskSchedulerBuilder threadPoolTaskSchedulerBuilder) {
        this.threadPoolTaskSchedulerBuilder = threadPoolTaskSchedulerBuilder;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = threadPoolTaskSchedulerBuilder != null ? threadPoolTaskSchedulerBuilder.build() : new ThreadPoolTaskScheduler();
        int size = taskRegistrar.getCronTaskList().size() + taskRegistrar.getFixedDelayTaskList().size() + taskRegistrar.getFixedRateTaskList().size() + taskRegistrar.getTriggerTaskList().size();
        taskScheduler.setPoolSize(size == 0 ? 1 : size);
        taskScheduler.setThreadFactory(Thread.ofVirtual().name("scheduling-",0).factory());
        taskScheduler.afterPropertiesSet();
        taskRegistrar.setScheduler(taskScheduler);
    }

}
