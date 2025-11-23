package io.github.dengchen2020.core.web.mvc;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * 注册处理静态资源的Servlet
 * @author xiaochen
 * @since 2025/8/1
 */
public class StaticServletBeanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    Environment environment;

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata,@NonNull BeanDefinitionRegistry registry) {
        String mvcServletPath = environment.getProperty("spring.mvc.servlet.path","/");
        String serverServletContextPath = environment.getProperty("server.servlet.context-path","");
        if ((!mvcServletPath.isBlank() && !"/".equals(mvcServletPath)) && (serverServletContextPath.isEmpty() || "/".equals(serverServletContextPath))) {
            registry.registerBeanDefinition("staticServlet", new RootBeanDefinition(StaticResourceServlet.class));
        }
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }
}
