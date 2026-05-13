package io.github.dengchen2020.jdbc.base;

import com.querydsl.sql.SQLTemplates;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * jdbc自动配置
 * @author xiaochen
 * @since 2024/6/3
 */
@ConditionalOnClass(SQLTemplates.class)
@Configuration(proxyBeanMethods = false)
public final class JdbcAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    BaseJdbcRepositoryFragmentsContributor baseJdbcRepositoryFragmentsContributor(DataSource dataSource) {
        return new BaseJdbcRepositoryFragmentsContributor(dataSource);
    }

}
