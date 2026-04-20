package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.github.dengchen2020.jdbc.base.CacheSupport.entityPathMap;

/**
 * JDBC操作通用接口实现
 * @author xiaochen
 * @since 2025/12/8
 */
@NullMarked
@Transactional(propagation = Propagation.SUPPORTS)
public class BaseJdbcRepositoryExecutor<T,ID> implements BaseJdbcRepository<T, ID>, JdbcClientRepository, JdbcRepositoryExtension<T, ID> {

    private final SQLQueryFactory queryFactory;
    private final JdbcClient jdbcClient;

    public BaseJdbcRepositoryExecutor(SQLQueryFactory queryFactory, JdbcClient jdbcClient) {
        this.queryFactory = queryFactory;
        this.jdbcClient = jdbcClient;
    }

    @SuppressWarnings("unchecked")
    protected RelationalPathBase<T> entityPath() {
        return (RelationalPathBase<T>) entityPathMap.computeIfAbsent(getDomainClass(), DcSqlEntityPathResolver.INSTANCE::createPath);
    }

    @SuppressWarnings("unchecked")
    private RelationalPathBase<T> entityPath(Class<T> domainClass) {
        return (RelationalPathBase<T>) entityPathMap.computeIfAbsent(domainClass, DcSqlEntityPathResolver.INSTANCE::createPath);
    }

    @Override
    public JdbcClient jdbcClient() {
        return jdbcClient;
    }

    public SQLQueryFactory nativeQueryFactory() {
        return queryFactory;
    }

    public SQLQuery<?> nativeQuery() {
        return queryFactory.query().from(entityPath());
    }

    @Override
    public <R> SQLQuery<R> nativeSelect(Expression<R> expr) {
        return queryFactory.select(expr).from(entityPath());
    }

    @Override
    public SQLQuery<Tuple> nativeSelect(Expression<?>... exprs) {
        return queryFactory.select(exprs).from(entityPath());
    }

    @Override
    public <R> SQLQuery<R> nativeSelectDistinct(Expression<R> expr) {
        return queryFactory.selectDistinct(expr).from(entityPath());
    }

    @Override
    public SQLQuery<Tuple> nativeSelectDistinct(Expression<?>... exprs) {
        return queryFactory.selectDistinct(exprs).from(entityPath());
    }

    @Override
    public SQLQuery<Integer> nativeSelectOne() {
        return queryFactory.selectOne().from(entityPath());
    }

    @Override
    public SQLQuery<Integer> nativeSelectZero() {
        return queryFactory.selectZero().from(entityPath());
    }

    @Override
    public SQLQuery<T> nativeSelectFrom() {
        var domainClass = getDomainClass();
        var entityPath = entityPath(domainClass);
        return queryFactory.select(Projections.bean(domainClass, entityPath.all())).from(entityPath);
    }

}
