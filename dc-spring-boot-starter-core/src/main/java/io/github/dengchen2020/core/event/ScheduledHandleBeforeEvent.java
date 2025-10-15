package io.github.dengchen2020.core.event;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 定时任务执行前回调
 *
 * @param joinPoint   ProceedingJoinPoint
 * @param localIpInfo 执行定时任务的服务器内网ip信息，192.168.1.100:8080
 * @author xiaochen
 * @since 2024/6/3
 */
public record ScheduledHandleBeforeEvent(ProceedingJoinPoint joinPoint, String localIpInfo) {

}
