package io.github.dengchen2020.jpa.config;

import com.querydsl.sql.SQLTemplates;
import io.github.dengchen2020.jpa.base.DcEvaluationContextExtension;
import io.github.dengchen2020.jpa.base.QuerydslJdbcRepositoryExecutor;
import io.github.dengchen2020.jpa.base.RepositoryFragmentsCustomizer;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

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

    @ConditionalOnMissingBean
    @Bean
    DcEvaluationContextExtension dcEvaluationContextExtension() {
        return new DcEvaluationContextExtension();
    }

    @ConditionalOnClass(SQLTemplates.class)
    @Configuration(proxyBeanMethods = false)
    static final class QuerydslJdbcAutoConfiguration {

        @ConditionalOnMissingBean
        @Bean
        RepositoryFragmentsCustomizer baseJdbcRepositoryFragmentsContributor(ObjectProvider<DataSource> dataSource) {
            return (metadata, entityInformation, entityManager, resolver) -> new QuerydslJdbcRepositoryExecutor<>(entityInformation, entityManager, resolver, dataSource.getIfAvailable());
        }

    }

}
