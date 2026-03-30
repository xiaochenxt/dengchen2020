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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    HibernatePropertiesCustomizer dcHibernatePropertiesCustomizer(@Value("${spring.profiles.active:null}") String active) {
        return hibernateProperties -> {
            if (!"dev".equals(active)) {
                // 禁用自动建表
                hibernateProperties.remove(AvailableSettings.HBM2DDL_AUTO);
                // 禁用启动时检查命名查询
                hibernateProperties.put("hibernate.query.startup_check", false);
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
