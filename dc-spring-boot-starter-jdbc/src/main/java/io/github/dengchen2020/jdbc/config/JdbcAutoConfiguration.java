package io.github.dengchen2020.jdbc.config;

import com.querydsl.sql.*;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.data.jdbc.autoconfigure.DataJdbcRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

/**
 * JDBC自动配置
 * @author xiaochen
 * @since 2025/12/8
 */
@Configuration(proxyBeanMethods = false)
public final class JdbcAutoConfiguration {

    /**
     * 参考：{@link DataJdbcRepositoriesAutoConfiguration.SpringBootJdbcConfiguration#jdbcCustomConversions()} 和 {@link AbstractJdbcConfiguration#jdbcCustomConversions()}
     */
    @ConditionalOnClass(name = "org.postgresql.util.PGobject")
    @Bean
    @ConditionalOnMissingBean
    JdbcCustomConversions jdbcCustomConversions(JdbcDialect dialect) {
        return JdbcConfiguration.createCustomConversions(dialect, List.of(new PGobjectToStringConverter()));
    }

//    /**
//     * SpringBoot4.0.0开始，引入了Spring Data Aot，aot时需要自行提供方言（后续官方会优化），详见：<a href="https://github.com/spring-projects/spring-boot/issues/48240">Spring Data JDBC 和 AOT 时需要 JdbcDialect</a>
//     * <a href="https://github.com/spring-projects/spring-boot/issues/47781">允许无需 DataSource 初始化 即可实现 Data JDBC 方言解析</a>
//     * @return
//     */
//    @Bean
//    JdbcPostgresDialect jdbcDialect() {
//        return JdbcPostgresDialect.INSTANCE;
//    }

    @ConditionalOnMissingBean
    @Bean
    SQLQueryFactory sqlQueryFactory(DataSource dataSource) throws SQLException {
        var provider = new Supplier<Connection>() {
            @Override
            public Connection get() {
                return DataSourceUtils.getConnection(dataSource);
            }
        };
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(new SQLTemplatesRegistry().getTemplates(DataSourceUtils.getConnection(dataSource).getMetaData()));
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        configuration.addListener(new SQLBaseListener(){
            static final String PARENT_CONTEXT = (AbstractSQLQuery.class.getName() + "#PARENT_CONTEXT").intern();
            @Override
            public void end(SQLListenerContext context) {
                var connection = context.getConnection();
                if (connection != null && context.getData(PARENT_CONTEXT) == null) {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            }
        });
        return new SQLQueryFactory(configuration, provider);
    }

    @Bean
    DcEntityCallback entityBeforeConvertCallback(RelationalMappingContext context) {
        return new DcEntityCallback<>(context);
    }

}
