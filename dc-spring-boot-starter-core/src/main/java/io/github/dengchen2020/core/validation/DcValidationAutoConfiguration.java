package io.github.dengchen2020.core.validation;

import org.hibernate.validator.BaseHibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.validation.autoconfigure.ValidationConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 注解校验自动配置
 * @author xiaochen
 * @since 2025/6/6
 */
@ConditionalOnClass({ValidationConfigurationCustomizer.class, ConfigurationImpl.class})
@ConditionalOnProperty(value = "dc.validation.fail-fast", matchIfMissing = true, havingValue = "true")
@Configuration(proxyBeanMethods = false)
public final class DcValidationAutoConfiguration {

    @Bean
    ValidationConfigurationCustomizer dcValidationConfigurationCustomizer(){
        return configuration -> {
            if (configuration instanceof ConfigurationImpl configurationImpl) {
                if (configurationImpl.getProperties().get(BaseHibernateValidatorConfiguration.FAIL_FAST) == null) {
                    configurationImpl.failFast(true);
                }
            }
        };
    }

}
