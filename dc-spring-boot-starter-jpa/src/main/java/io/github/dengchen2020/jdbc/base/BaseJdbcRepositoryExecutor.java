package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplatesRegistry;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * JDBC操作通用接口实现
 * @author xiaochen
 * @since 2025/12/8
 */
@NullMarked
@Transactional(propagation = Propagation.SUPPORTS)
public class BaseJdbcRepositoryExecutor<T,ID> implements BaseJdbcRepository<T, ID> {

    private final NativeQueryFactory queryFactory;
    private final EntityPath<T> path;

    public BaseJdbcRepositoryExecutor(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager, EntityPathResolver resolver, DataSource dataSource) throws SQLException {
        this.path = resolver.createPath(entityInformation.getJavaType());
        this.queryFactory = new NativeQueryFactory(entityManager, new SQLTemplatesRegistry().getTemplates(DataSourceUtils.getConnection(dataSource).getMetaData()));
    }

    @Override
    public <R> JPASQLQuery<R> nativeSelect(Expression<R> expr) {
        return queryFactory.select(expr).from(path);
    }

    @Override
    public JPASQLQuery<Tuple> nativeSelect(Expression<?>... exprs) {
        return queryFactory.select(exprs).from(path);
    }

}
