package io.github.dengchen2020.jdbc.config;

import com.querydsl.sql.*;
import com.querydsl.sql.spring.SpringExceptionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.core.mapping.JdbcSimpleTypes;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.mapping.model.SimpleTypeHolder;
import org.springframework.data.relational.core.dialect.Dialect;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * JDBC自动配置
 * @author xiaochen
 * @since 2025/12/8
 */
@Configuration(proxyBeanMethods = false)
public final class JdbcAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(JdbcAutoConfiguration.class);

    /**
     * 参考：{@link JdbcRepositoriesAutoConfiguration.SpringBootJdbcConfiguration#jdbcCustomConversions()} 和 {@link AbstractJdbcConfiguration#jdbcCustomConversions()}
     */
    @ConditionalOnClass(name = "org.postgresql.util.PGobject")
    @ConditionalOnMissingBean
    @Bean
    JdbcCustomConversions jdbcCustomConversions(Dialect dialect) {
        try {
            SimpleTypeHolder simpleTypeHolder = dialect.simpleTypes().isEmpty() ? JdbcSimpleTypes.HOLDER
                    : new SimpleTypeHolder(dialect.simpleTypes(), JdbcSimpleTypes.HOLDER);
            List<Object> converters = new ArrayList<>();
            converters.addAll(dialect.getConverters());
            converters.addAll(JdbcCustomConversions.storeConverters());
            return new JdbcCustomConversions(
                    CustomConversions.StoreConversions.of(simpleTypeHolder, converters), List.of(new PGobjectToStringConverter()));
        } catch (NoSuchBeanDefinitionException exception) {
            log.warn("No dialect found; CustomConversions will be configured without dialect specific conversions");
            return new JdbcCustomConversions();
        }
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
