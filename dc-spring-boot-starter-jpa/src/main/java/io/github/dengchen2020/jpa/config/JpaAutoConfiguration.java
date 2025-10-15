package io.github.dengchen2020.jpa.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * jpa自动配置
 * @author xiaochen
 * @since 2024/6/3
 */
@Configuration(proxyBeanMethods = false)
public class JpaAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public JPAQueryFactory jpaQueryFactory(EntityManager entityManager){
        return new JPAQueryFactory(entityManager);
    }

    @Bean
    public HibernatePropertiesCustomizer dcHibernatePropertiesCustomizer(@Value("${spring.profiles.active:null}") String active) {
        return hibernateProperties -> {
            if (!"dev".equals(active)) hibernateProperties.remove(AvailableSettings.HBM2DDL_AUTO);
        };
    }

}
