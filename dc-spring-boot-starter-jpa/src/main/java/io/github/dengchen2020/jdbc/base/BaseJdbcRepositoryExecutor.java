package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplatesRegistry;
import io.github.dengchen2020.jpa.base.JpaRepositoryExtension;
import io.github.dengchen2020.jpa.querydsl.EntityPathCacheSupport;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NullMarked;
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
public class BaseJdbcRepositoryExecutor<T,ID> implements BaseJdbcRepository<T, ID>, JpaRepositoryExtension<T, ID> {

    private final NativeQueryFactory queryFactory;

    public BaseJdbcRepositoryExecutor(EntityManager entityManager, DataSource dataSource) throws SQLException {
        this.queryFactory = new NativeQueryFactory(entityManager, new SQLTemplatesRegistry().getTemplates(DataSourceUtils.getConnection(dataSource).getMetaData()));
    }

    @SuppressWarnings("unchecked")
    protected EntityPath<T> entityPath() {
        return (EntityPath<T>) EntityPathCacheSupport.getEntityPath(getDomainClass());
    }

    @Override
    public <R> JPASQLQuery<R> nativeSelect(Expression<R> expr) {
        return queryFactory.select(expr).from(entityPath());
    }

    @Override
    public JPASQLQuery<Tuple> nativeSelect(Expression<?>... exprs) {
        return queryFactory.select(exprs).from(entityPath());
    }

}
