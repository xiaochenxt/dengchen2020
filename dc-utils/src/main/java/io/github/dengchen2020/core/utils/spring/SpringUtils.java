package io.github.dengchen2020.core.utils.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * Spring工具类，谨慎使用，容易导致将启动期的错误延迟到运行时
 * @author xiaochen
 * @since 2025/12/23
 */
public abstract class SpringUtils {

    static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        if (applicationContext == null) throw new IllegalStateException("Spring应用上下文未初始化");
        return applicationContext;
    }

    public static Environment getEnvironment() {
        return getApplicationContext().getEnvironment();
    }

    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    public static void publishEvent(Object event) {
        getApplicationContext().publishEvent(event);
    }

}
