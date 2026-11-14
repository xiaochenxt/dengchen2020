package io.github.dengchen2020.core.retry;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.retry.annotation.RetryConfiguration;

import java.lang.annotation.*;

/**
 * 等价于 @EnableRetry，但不启用 @EnableAspectJAutoProxy（因此不需要 aspectjweaver）
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EnableResilientMethods.Registrar.class)
public @interface EnableResilientMethods {

    /**
     * 重试 Advisor 的顺序，数值越小越靠外层（应位于事务 Advisor 外层）
     */
    int order() default Ordered.LOWEST_PRECEDENCE - 1;

    class Registrar implements ImportBeanDefinitionRegistrar {
        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            var attributes = AnnotationAttributes.fromMap(
                    importingClassMetadata.getAnnotationAttributes(EnableResilientMethods.class.getName()));
            var definition = new RootBeanDefinition(RetryConfiguration.class);
            definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            definition.getPropertyValues().add("order", attributes.getNumber("order"));
            registry.registerBeanDefinition(RetryConfiguration.class.getName(), definition);
        }
    }
}
