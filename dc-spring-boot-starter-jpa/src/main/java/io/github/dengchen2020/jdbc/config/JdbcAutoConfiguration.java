package io.github.dengchen2020.jdbc.config;

import com.querydsl.sql.*;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * JDBC自动配置
 * @author xiaochen
 * @since 2025/12/8
 */
@ConditionalOnClass(SQLQueryFactory.class)
@Configuration(proxyBeanMethods = false)
public final class JdbcAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    SQLQueryFactory sqlQueryFactory(DataSource dataSource) throws SQLException {
        com.querydsl.sql.Configuration configuration = new com.querydsl.sql.Configuration(new SQLTemplatesRegistry().getTemplates(DataSourceUtils.getConnection(dataSource).getMetaData()));
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        configuration.addListener(new SpringSQLCloseListener(dataSource));
        return new SQLQueryFactory(configuration, () -> DataSourceUtils.getConnection(dataSource));
    }

    public final static class SpringSQLCloseListener extends SQLBaseListener {
        static final String PARENT_CONTEXT = (AbstractSQLQuery.class.getName() + "#PARENT_CONTEXT").intern();

        private final DataSource dataSource;

        public SpringSQLCloseListener(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public void end(SQLListenerContext context) {
            var connection = context.getConnection();
            if (connection != null && context.getData(PARENT_CONTEXT) == null) {
                DataSourceUtils.releaseConnection(connection, dataSource);
            }
        }
    }

}
