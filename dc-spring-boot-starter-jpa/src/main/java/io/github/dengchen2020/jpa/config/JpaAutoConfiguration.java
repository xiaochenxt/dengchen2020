package io.github.dengchen2020.jpa.config;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.sql.SQLTemplatesRegistry;
import io.github.dengchen2020.jpa.querydsl.NativeQueryFactory;
import jakarta.persistence.EntityManager;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.jdbc.datasource.DataSourceUtils;

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
    JPAQueryFactory jpaQueryFactory(EntityManager entityManager) {
        var provider = PersistenceProvider.fromEntityManager(entityManager);
        var templates = switch (provider) {
            case ECLIPSELINK -> EclipseLinkTemplates.DEFAULT;
            case HIBERNATE -> HQLTemplates.DEFAULT;
            default -> JPQLTemplates.DEFAULT;
        };
        return new JPAQueryFactory(templates, entityManager);
    }

    @ConditionalOnMissingBean
    @Bean
    NativeQueryFactory nativeQueryFactory(EntityManager entityManager, DataSource dataSource) throws SQLException {
        return new NativeQueryFactory(entityManager, new SQLTemplatesRegistry().getTemplates(DataSourceUtils.getConnection(dataSource).getMetaData()));
    }

}
