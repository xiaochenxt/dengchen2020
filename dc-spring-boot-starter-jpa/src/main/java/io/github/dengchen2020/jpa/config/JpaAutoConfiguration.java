package io.github.dengchen2020.jpa.config;

import io.github.dengchen2020.jpa.base.BaseJpaRepositoryFragmentsContributor;
import io.github.dengchen2020.jpa.base.DcEvaluationContextExtension;
import io.github.dengchen2020.jpa.base.QuerydslJpaRepositoryFragmentsContributor;
import org.hibernate.cfg.AvailableSettings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFragmentsContributor;

import java.util.List;

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

    @Bean
    static BeanPostProcessor dcJpaBeanPostProcessor(List<JpaRepositoryFragmentsContributor> jpaRepositoryFragmentsContributors) {
        return new BeanPostProcessor() {
            @Override
            public @NonNull Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof JpaRepositoryFactoryBean<?,?,?> jpaRepositoryFactoryBean) {
                    if (bean.getClass() != JpaRepositoryFactoryBean.class) return bean;
                    var contributor = BaseJpaRepositoryFragmentsContributor.INSTANCE.andThen(QuerydslJpaRepositoryFragmentsContributor.INSTANCE);
                    for (var fragmentsContributor : jpaRepositoryFragmentsContributors) {
                        contributor = contributor.andThen(fragmentsContributor);
                    }
                    jpaRepositoryFactoryBean.setRepositoryFragmentsContributor(contributor.andThen(JpaRepositoryFragmentsContributor.DEFAULT));
                }
                return bean;
            }
        };
    }

}
