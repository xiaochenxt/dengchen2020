package io.github.dengchen2020.jpa.base;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import com.querydsl.sql.*;
import io.github.dengchen2020.core.jdbc.Page;
import io.github.dengchen2020.core.jdbc.SimplePage;
import io.github.dengchen2020.jpa.querydsl.NativeQuery;
import io.github.dengchen2020.jpa.querydsl.NativeQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.stream.Stream;
import org.hibernate.Session;
import org.hibernate.dialect.*;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 使用Querydsl实现
 *
 * @author xiaochen
 * @since 2025/3/28
 */
@NullMarked
@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class QuerydslJpaRepositoryExecutor<T, ID> extends SimpleJpaRepository<T, ID> implements QuerydslJpaRepository<T>, ComplexJpaRepository<T> {

    protected final EntityManager entityManager;

    protected  final JPAQueryFactory queryFactory;
    protected final NativeQueryFactory nativeQueryFactory;
    protected final NativeQuery<T> nativeQuery;

    protected final EntityPath<T> path;
    protected final PathBuilder<T> builder;
   // protected final Querydsl querydsl;

    public QuerydslJpaRepositoryExecutor(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.path = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
        this.builder = new PathBuilder<>(path.getType(), path.getMetadata());
        var querydsl = new Querydsl(entityManager, builder);
        this.queryFactory = new JPAQueryFactory(querydsl.getTemplates(), entityManager);
        this.nativeQueryFactory = new NativeQueryFactory(entityManager, getTemplates(entityManager));
        this.nativeQuery = new NativeQuery<>(nativeQueryFactory, path);
    }

    private static SQLTemplates getTemplates(EntityManager entityManager) {
        SessionFactoryImplementor sessionFactory = (SessionFactoryImplementor) entityManager.unwrap(Session.class).getSessionFactory();
        var dialect = sessionFactory.getJdbcServices().getDialect();
        return switch (dialect) {
            case H2Dialect _ -> H2Templates.DEFAULT;
            case HSQLDialect _ -> HSQLDBTemplates.DEFAULT;
            case MySQLDialect _ -> MySQLTemplates.DEFAULT;
            case OracleDialect _ -> OracleTemplates.DEFAULT;
            case PostgreSQLDialect _ -> PostgreSQLTemplates.DEFAULT;
            case SQLServerDialect _ -> {
                var databaseMajorVersion = dialect.getVersion().getMajor();
                if (databaseMajorVersion < 9) yield SQLServerTemplates.DEFAULT;
                if (databaseMajorVersion == 9) yield SQLServer2005Templates.DEFAULT;
                if (databaseMajorVersion == 10) yield SQLServer2008Templates.DEFAULT;
                yield SQLServer2012Templates.DEFAULT;
            }
            case null, default -> SQLTemplates.DEFAULT;
        };
    }

    public JPAQueryFactory queryFactory() {
        return queryFactory;
    }

    /**
     * 原生SQL查询工厂，与{@link #nativeQuery}不同的是，不自带from当前实体对应的表。原生SQL支持子查询、CTE、窗口函数等复杂SQL，更加灵活自由
     * @return {@link NativeQuery<T>}
     */
    public NativeQueryFactory nativeQueryFactory() {
        return nativeQueryFactory;
    }

    /**
     * 原生SQL查询，所有的操作自带from当前实体对应的表，不允许再from其他表，否则形成笛卡尔积查询
     * @return {@link NativeQuery<T>}
     */
    public NativeQuery<T> nativeQuery() {
        return nativeQuery;
    }

    public PathBuilder<T> builder() {
        return builder;
    }

    public EntityPath<T> path() {
        return path;
    }

    public JPAQuery<?> query() {
        return queryFactory.query().from(path);
    }

    @Override
    public <R> JPAQuery<R> select(Expression<R> expr) {
        return queryFactory.select(expr).from(path);
    }

    @Override
    public JPAQuery<Tuple> select(Expression<?>... exprs) {
        return queryFactory.select(exprs).from(path);
    }

    @Override
    public <R> JPAQuery<R> selectDistinct(Expression<R> expr) {
        return queryFactory.selectDistinct(expr).from(path);
    }

    @Override
    public JPAQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return queryFactory.selectDistinct(exprs).from(path);
    }

    @Override
    public JPAQuery<Integer> selectOne() {
        return queryFactory.selectOne().from(path);
    }

    @Override
    public JPAQuery<Integer> selectZero() {
        return queryFactory.selectZero().from(path);
    }

    @Override
    public JPAQuery<T> selectFrom() {
        return queryFactory.selectFrom(path);
    }

    @Override
    public JPAUpdateClause update(Predicate[] where) {
        Assert.notEmpty(where, "更新必须有条件");
        return queryFactory.update(path).where(where);
    }

    @Override
    public long delete(Predicate[] where) {
        Assert.notEmpty(where, "删除必须有条件");
        return queryFactory.delete(path).where(where).execute();
    }

    @Override
    public <R> SimplePage<R> fetchPage(JPAQuery<R> query, Page page, OrderSpecifier<?>... o){
        if (page.getSize() == 0) return new SimplePage<>(!page.isSelectCount() ? 0 : query.fetchCount(), Collections.emptyList());
        if (o.length > 0) query = query.orderBy(o);
        if (!page.isSelectCount()) {
            return new SimplePage<>(null, query.limit(page.getSize())
                    .offset(page.getOffset())
                    .fetch());
        }
        QueryResults<R> result = query.limit(page.getSize())
                .offset(page.getOffset())
                .fetchResults();
        return new SimplePage<>(result.getTotal(), result.getResults());
    }

    @Override
    public SimplePage<T> findAll(Predicate predicate, Page page, OrderSpecifier<?>... o){
        return fetchPage(selectFrom().where(predicate), page, o);
    }

    @Override
    public <R> Stream<R> fetchStream(JPAQuery<R> query,@Nullable Page page, OrderSpecifier<?>... o){
        if (o.length > 0) query = query.orderBy(o);
        if(page == null) return query.stream();
        return query.limit(page.getSize())
                .offset(page.getOffset())
                .stream();
    }

    @Override
    public Stream<T> findStream(Predicate predicate,@Nullable Page page, OrderSpecifier<?>... o){
        return fetchStream(selectFrom().where(predicate), page, o);
    }

    @Override
    public Stream<T> findStream(@Nullable Page page, OrderSpecifier<?>... o){
        return fetchStream(selectFrom(), page, o);
    }

    @Override
    public Stream<T> findStream(OrderSpecifier<?>... o){
        return fetchStream(selectFrom(), null, o);
    }

}
