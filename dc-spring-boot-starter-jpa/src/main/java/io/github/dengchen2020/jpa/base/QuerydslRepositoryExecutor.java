package io.github.dengchen2020.jpa.base;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
import io.github.dengchen2020.core.jdbc.Page;
import io.github.dengchen2020.core.jdbc.SimplePage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.stream.Stream;

/**
 * 使用Querydsl实现
 *
 * @author xiaochen
 * @since 2025/3/28
 */
@NullMarked
@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class QuerydslRepositoryExecutor<T, ID> extends SimpleJpaRepository<T, ID> implements QueryDslJpaRepository<T>, ComplexJpaRepository<T> {

    protected final EntityManager entityManager;

    protected  final JPAQueryFactory queryFactory;

    protected final EntityPath<T> path;
    protected final PathBuilder<T> builder;
   // protected final Querydsl querydsl;

    public QuerydslRepositoryExecutor(JpaEntityInformation<T, ?> entityInformation, EntityManager em, JPAQueryFactory queryFactory) {
        super(entityInformation, em);
        this.entityManager = em;
        this.queryFactory = queryFactory;
        this.path = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
        this.builder = new PathBuilder<>(path.getType(), path.getMetadata());
       // this.querydsl = new Querydsl(entityManager, builder);
    }

    /**
     * 为给定的 {@link Predicate} 创建一个新的 {@link JPAQuery}。
     *
     * @param predicate
     * @return {@link JPAQuery}.
     */
    protected JPAQuery<?> createQuery(Predicate... predicate) {
        JPAQuery<?> query = queryFactory.query().from(path);
        if (predicate.length > 0) query.where(predicate);
        getQueryHints().withFetchGraphs(entityManager).forEach(query::setHint);
        CrudMethodMetadata metadata = super.getRepositoryMethodMetadata();
        if (metadata == null) return query;
        LockModeType type = metadata.getLockModeType();
        return type == null ? query : query.setLockMode(type);
    }

    public JPAQueryFactory queryFactory() {
        return queryFactory;
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
    public JPAUpdateClause update(Predicate where) {
        return update(new Predicate[]{where});
    }

    @Override
    public long delete(Predicate[] where) {
        Assert.notEmpty(where, "删除必须有条件");
        return queryFactory.delete(path).where(where).execute();
    }

    @Override
    public long delete(Predicate where) {
        return delete(new Predicate[]{where});
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
