package io.github.dengchen2020.jdbc.config;

import com.querydsl.sql.*;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.ClassUtils;

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
public final class JdbcAutoConfiguration extends AbstractJdbcConfiguration {

    public static final boolean pGobjectPresent = ClassUtils.isPresent("org.postgresql.util.PGobject", JdbcAutoConfiguration.class.getClassLoader());

    @Override
    protected List<?> userConverters() {
        if (pGobjectPresent) return List.of(new PGobjectToStringConverter());
        return super.userConverters();
    }

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
