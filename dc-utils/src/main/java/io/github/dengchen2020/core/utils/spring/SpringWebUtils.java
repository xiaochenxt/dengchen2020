package io.github.dengchen2020.core.utils.spring;

import io.github.dengchen2020.core.utils.RequestUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * SpringWeb环境中可使用的工具类，谨慎使用，容易导致启动期的错误延迟到运行时
 * @author xiaochen
 * @since 2025/12/23
 */
@NullMarked
public abstract class SpringWebUtils {

    public static WebApplicationContext getWebApplicationContext() {
        var webApplicationContext = RequestContextUtils.findWebApplicationContext(RequestUtils.getCurrentRequest());
        if (webApplicationContext == null) throw new IllegalStateException("未处于spring-web环境中，无法获取webApplicationContext");
        return webApplicationContext;
    }

    public static Environment getEnvironment() {
        return getWebApplicationContext().getEnvironment();
    }

    public static <T> T getBean(Class<T> clazz) {
        return getWebApplicationContext().getBean(clazz);
    }

    public static void publishEvent(Object event) {
        getWebApplicationContext().publishEvent(event);
    }

}
