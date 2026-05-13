package io.github.dengchen2020.jpa.config;

import com.querydsl.sql.SQLTemplates;
import io.github.dengchen2020.jpa.base.BaseJpaRepositoryFragmentsContributor;
import io.github.dengchen2020.jpa.base.DcEvaluationContextExtension;
import io.github.dengchen2020.jpa.base.QuerydslJdbcRepositoryFragmentsContributor;
import io.github.dengchen2020.jpa.base.QuerydslJpaRepositoryFragmentsContributor;
import org.hibernate.cfg.AvailableSettings;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFragmentsContributor;

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
        QuerydslJdbcRepositoryFragmentsContributor baseJdbcRepositoryFragmentsContributor(ObjectProvider<DataSource> dataSource) {
            return new QuerydslJdbcRepositoryFragmentsContributor(dataSource.getIfAvailable());
        }

    }

    @Bean
    static BeanPostProcessor dcJpaBeanPostProcessor(ObjectProvider<JpaRepositoryFragmentsContributor> jpaRepositoryFragmentsContributors) {
        return new DcJpaBeanPostProcessor(jpaRepositoryFragmentsContributors);
    }

    /**
     * 注册JPA存储库片段
     */
    static class DcJpaBeanPostProcessor implements BeanPostProcessor {

        private final ObjectProvider<JpaRepositoryFragmentsContributor> jpaRepositoryFragmentsContributors;

        DcJpaBeanPostProcessor(ObjectProvider<JpaRepositoryFragmentsContributor> jpaRepositoryFragmentsContributors) {
            this.jpaRepositoryFragmentsContributors = jpaRepositoryFragmentsContributors;
        }

        @Override
        public @NonNull Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof JpaRepositoryFactoryBean<?,?,?> jpaRepositoryFactoryBean) {
                if (bean.getClass() != JpaRepositoryFactoryBean.class) return bean; // 如果不是默认的JpaRepositoryFactoryBean就跳过
                var contributor = BaseJpaRepositoryFragmentsContributor.INSTANCE.andThen(QuerydslJpaRepositoryFragmentsContributor.INSTANCE);
                for (var fragmentsContributor : jpaRepositoryFragmentsContributors) {
                    contributor = contributor.andThen(fragmentsContributor);
                }
                jpaRepositoryFactoryBean.setRepositoryFragmentsContributor(contributor.andThen(JpaRepositoryFragmentsContributor.DEFAULT));
            }
            return bean;
        }

    }

}
