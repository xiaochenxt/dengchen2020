package io.github.dengchen2020.core.validation;

import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.validation.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注解校验自动配置
 * @author xiaochen
 * @since 2025/6/6
 */
@ConditionalOnProperty(value = "dc.validation.fail-fast", matchIfMissing = true, havingValue = "true")
@Configuration(proxyBeanMethods = false)
public class DcValidationAutoConfiguration {

    @Bean
    public ValidationConfigurationCustomizer dcValidationConfigurationCustomizer(){
        return configuration -> {
            if (configuration instanceof ConfigurationImpl configurationImpl) {
                if (configurationImpl.getProperties().get(BaseHibernateValidatorConfiguration.FAIL_FAST) == null) {
                    configurationImpl.failFast(true);
                }
            }
        };
    }

}
