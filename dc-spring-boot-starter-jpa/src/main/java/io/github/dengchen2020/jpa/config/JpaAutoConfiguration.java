package io.github.dengchen2020.jpa.config;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * jpa自动配置
 * @author xiaochen
 * @since 2024/6/3
 */
@Configuration(proxyBeanMethods = false)
public final class JpaAutoConfiguration {

    @Bean
    HibernatePropertiesCustomizer dcHibernatePropertiesCustomizer(Environment environment) {
        return hibernateProperties -> {
            var active = environment.getActiveProfiles();
            if (active.length != 1 || !"dev".equals(active[0])) { // 如果只启用了一个环境配置并且是开发环境，此时才允许自动建表
                // 禁用自动建表
                hibernateProperties.remove(AvailableSettings.HBM2DDL_AUTO);
                // 禁用启动时检查命名查询
                hibernateProperties.put("hibernate.query.startup_check", false);
            }
            if (environment.getProperty("dc.jpa.properties.hibernate.dynamic-insert", Boolean.class, true)) {
                hibernateProperties.put("dc.hibernate.dynamic_insert", true);
            }
            if (environment.getProperty("dc.jpa.properties.hibernate.dynamic-update", Boolean.class, true)) {
                hibernateProperties.put("dc.hibernate.dynamic_update", true);
            }
        };
    }

}
