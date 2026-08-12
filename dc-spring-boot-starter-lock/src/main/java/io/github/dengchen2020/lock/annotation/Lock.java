package io.github.dengchen2020.lock.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 锁注解
 * @author xiaochen
 * @since 2024/7/1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Lock {

    /**
     * 锁的key，为空默认为目标方法的toString()值，支持SpringEl表达式（如#p0，#a0，#obj.id，#id等）解析方法参数转化为key
     */
    String value() default "";

    /**
     * 锁定的资源名称
     */
    String name() default "";

    /**
     * 等待获取锁的时长
     */
    long waitTime() default -1;

    /**
     * 持有锁的时长（为-1将触发无限续期，执行完成或锁过期才会释放锁）
     */
    long lockTime() default -1;

    /**
     * 时间单位，默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * 异常提示
     */
    String errorMsg() default "请求人数过多，请稍后再试";

}
