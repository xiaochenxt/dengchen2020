package io.github.dengchen2020.core.web.mvc;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author xiaochen
 * @since 2025/8/1
 */
public class StaticServletBeanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    Environment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        String mvcServletPath = environment.getProperty("spring.mvc.servlet.path","/");
        String serverServletContextPath = environment.getProperty("server.servlet.context-path","");
        if (!"/".equals(mvcServletPath) && serverServletContextPath.isEmpty()) {
            registry.registerBeanDefinition("staticServlet", new RootBeanDefinition(StaticResourceServlet.class));
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
