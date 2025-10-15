package io.github.dengchen2020.core.scheduled;

import org.springframework.aot.hint.annotation.Reflective;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务注解
 *
 * @author xiaochen
 * @since 2022/12/14
 */
@Reflective
@Inherited
@Scheduled
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DcScheduled {

    /**
     * 定时任务表达式
     */
    @AliasFor(value = "cron", annotation = Scheduled.class)
    String value() default "";

    /**
     * 定时任务表达式
     */
    @AliasFor(annotation = Scheduled.class)
    String cron() default "";

    /**
     * 将解析 cron 表达式的时区。默认情况下，此
     * 属性是空的字符串（即将使用调度程序的时区）。
     * @return 接受者的区域 ID {@link java.util.TimeZone#getTimeZone(String)},
     * 或空 String 以指示调度程序的默认时区
     * @since 4.0
     * @see org.springframework.scheduling.support.CronTrigger#CronTrigger(String, java.util.TimeZone)
     * @see java.util.TimeZone
     */
    @AliasFor(annotation = Scheduled.class)
    String zone() default "";

    /**
     * 在上次调用结束和下一次调用开始之间以固定的周期执行带注释的方法。
     * 时间单位默认为毫秒，可以通过timeUnit覆盖
     */
    @AliasFor(annotation = Scheduled.class)
    long fixedDelay() default -1;

    /**
     * 距离上一次执行完后多久执行
     */
    @AliasFor(annotation = Scheduled.class)
    String fixedDelayString() default "";

    /**
     * 在两次调用之间有固定的时间段执行带注释的方法。
     * <p>时间单位默认为毫秒，但可以通过以下方式覆盖
     * {@link #timeUnit}.
     * @return 期间
     */
    @AliasFor(annotation = Scheduled.class)
    long fixedRate() default -1;

    /**
     * 在两次调用之间有固定的时间段执行带注释的方法。
     * <p>时间单位默认为毫秒，但可以通过以下方式覆盖
     * {@link #timeUnit}.
     * <p>此属性变体支持 Spring 样式的"${...}"占位符
     * 以及 SpEL 表达式。
     * @return 作为 String 值的句点 — 例如，占位符
     * 或 a{@link java.time.Duration#parse java.time.Duration} 合规值
     * @since 3.2.2
     * @see #fixedRate()
     */
    @AliasFor(annotation = Scheduled.class)
    String fixedRateString() default "";

    /**
     * 在首次执行 a 之前延迟的时间单位数
     * {@link #fixedRate} 或 {@link #fixedDelay} task.
     * <p>默认情况下，时间单位为毫秒，但可以通过以下方式覆盖
     * {@link #timeUnit}.
     * @return the initial
     * @since 3.2
     */
    @AliasFor(annotation = Scheduled.class)
    long initialDelay() default -1;

    /**
     * 在首次执行 a 之前延迟的时间单位数
     * {@link #fixedRate} 或 {@link #fixedDelay} 任务.
     * <p>默认情况下，时间单位为毫秒，但可以通过以下方式覆盖
     * {@link #timeUnit}.
     * <p>此属性变体支持 Spring 样式 "${...}" 占位符
     * 以及 SpEL 表达式。
     * @return 作为 String 值的初始延迟 — 例如，占位符
     * 或 a {@link java.time.Duration#parse java.time.Duration} 合规值
     * @since 3.2.2
     * @see #initialDelay()
     */
    @AliasFor(annotation = Scheduled.class)
    String initialDelayString() default "";

    /**
     * {@link TimeUnit} 用于 {@link #fixedDelay}, {@link #fixedDelayString},
     * {@link #fixedRate}, {@link #fixedRateString}, {@link #initialDelay},
     * {@link #initialDelayString}.
     * <p>默认为{@link TimeUnit#MILLISECONDS}.
     * <p>对于以下情况，将忽略此属性{@linkplain #cron() cron 表达式}
     * 以及 {@link java.time.Duration} 通过以下方式提供的值 {@link #fixedDelayString},
     * {@link #fixedRateString}, {@link #initialDelayString}.
     * @return 要使用 {@code TimeUnit}
     * @since 5.3.10
     */
    @AliasFor(annotation = Scheduled.class)
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 用于确定要在其上运行此计划方法的计划程序的限定符。
     * <p>默认为空 String，建议默认计划程序。
     * <p>可用于确定要使用的目标调度器，
     * 匹配特定值（或 Bean 名称）的限定符值
     * {@link org.springframework.scheduling.TaskScheduler} 或
     * {@link java.util.concurrent.ScheduledExecutorService} Bean 定义。
     * @since 6.1
     * @see org.springframework.scheduling.SchedulingAwareRunnable#getQualifier()
     */
    @AliasFor(annotation = Scheduled.class)
    String scheduler() default "";

    /**
     * 允许多台服务器同时执行（默认false）
     */
    boolean concurrency() default false;

    /**
     * 指定时间内不允许重复执行，默认20秒
     */
    long seconds() default 20;

}
