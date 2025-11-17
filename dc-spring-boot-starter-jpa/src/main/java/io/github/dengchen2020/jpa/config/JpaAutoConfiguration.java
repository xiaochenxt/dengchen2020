package io.github.dengchen2020.jpa.config;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.impl.JPAProvider;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.provider.PersistenceProvider;

/**
 * jpa自动配置
 * @author xiaochen
 * @since 2024/6/3
 */
@Configuration(proxyBeanMethods = false)
public final class JpaAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    JPAQueryFactory jpaQueryFactory(EntityManager entityManager){
        var provider = PersistenceProvider.fromEntityManager(entityManager);
        var templates = switch (provider) {
            case ECLIPSELINK -> EclipseLinkTemplates.DEFAULT;
            case HIBERNATE -> HQLTemplates.DEFAULT;
            default -> JPAProvider.getTemplates(entityManager);
        };
        return new JPAQueryFactory(templates, entityManager);
    }

    @Bean
    HibernatePropertiesCustomizer dcHibernatePropertiesCustomizer(@Value("${spring.profiles.active:null}") String active) {
        return hibernateProperties -> {
            if (!"dev".equals(active)) hibernateProperties.remove(AvailableSettings.HBM2DDL_AUTO);
        };
    }

}
