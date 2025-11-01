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
import io.github.dengchen2020.core.jdbc.Page;
import io.github.dengchen2020.core.jdbc.SimplePage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
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

    protected final JpaEntityInformation<T, ?> entity;

    protected final PersistenceProvider provider;

    protected final EntityPath<T> path;
    protected final PathBuilder<T> builder;
    protected final Querydsl querydsl;

    public QuerydslRepositoryExecutor(JpaEntityInformation<T, ?> entityInformation, EntityManager em, JPAQueryFactory queryFactory) {
        super(entityInformation, em);
        this.entityManager = em;
        this.queryFactory = queryFactory;
        this.entity = entityInformation;
        this.provider = PersistenceProvider.fromEntityManager(em);
        this.path = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
        this.builder = new PathBuilder<>(path.getType(), path.getMetadata());
        this.querydsl = new Querydsl(entityManager, builder);
    }

    protected Map<String, Object> getHints() {
        Map<String, Object> hints = new HashMap<>();
        getQueryHints().withFetchGraphs(entityManager).forEach(hints::put);
        CrudMethodMetadata metadata = super.getRepositoryMethodMetadata();
        if (metadata != null) applyComment(metadata, hints::put);
        return hints;
    }

    private void applyComment(CrudMethodMetadata metadata, BiConsumer<String, Object> consumer) {
        if (metadata.getComment() != null && provider.getCommentHintKey() != null) {
            consumer.accept(provider.getCommentHintKey(), provider.getCommentHintValue(metadata.getComment()));
        }
    }

    protected void applyQueryHintsForCount(Query query) {
        CrudMethodMetadata metadata = super.getRepositoryMethodMetadata();
        if (metadata == null) return;
        getQueryHintsForCount().forEach(query::setHint);
        applyComment(metadata, query::setHint);
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

    public EntityManager entityManager() {
        return entityManager;
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

    /**
     * 单表数据查询
     * @return JPAQuery<T>
     */
    @Override
    public JPAQuery<T> selectFrom() {
        return queryFactory.selectFrom(path);
    }

    /**
     * 更新构造
     *
     * @param where 更新条件
     * @return JPAUpdateClause
     */
    @Override
    public JPAUpdateClause update(Predicate[] where) {
        Assert.notEmpty(where, "更新必须有条件");
        return queryFactory.update(path).where(where);
    }

    /**
     * 更新构造
     *
     * @param where 更新条件
     * @return JPAUpdateClause
     */
    @Override
    public JPAUpdateClause update(Predicate where) {
        return update(new Predicate[]{where});
    }

    /**
     * 删除构造
     *
     * @param where 删除条件
     * @return 受影响的行数
     */
    @Override
    public long delete(Predicate[] where) {
        Assert.notEmpty(where, "删除必须有条件");
        return queryFactory.delete(path).where(where).execute();
    }

    /**
     * 删除构造
     *
     * @param where 删除条件
     * @return 受影响的行数
     */
    @Override
    public long delete(Predicate where) {
        return delete(new Predicate[]{where});
    }

    /**
     * Querydsl分页查询
     * @param query JPAQuery<R>
     * @param page 分页参数
     * @param o 排序方式
     * @return SimplePage<R>
     */
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

    /**
     * Querydsl分页条件查询
     *
     * @param page 分页参数
     * @param o     排序方式
     * @return 分页后的数据
     */
    @Override
    public SimplePage<T> findAll(Predicate predicate, Page page, OrderSpecifier<?>... o){
        return fetchPage(selectFrom().where(predicate), page, o);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param query JPAQuery<R>
     * @param page 查询参数
     * @param o 排序方式
     * @return Stream<R>
     */
    @Override
    public <R> Stream<R> fetchStream(JPAQuery<R> query,@Nullable Page page, OrderSpecifier<?>... o){
        if (o.length > 0) query = query.orderBy(o);
        if(page == null) return query.stream();
        return query.limit(page.getSize())
                .offset(page.getOffset())
                .stream();
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param predicate 条件
     * @param page 查询参数
     * @param o 排序方式
     * @return Stream<T>
     */
    @Override
    public Stream<T> findStream(Predicate predicate, Page page, OrderSpecifier<?>... o){
        return fetchStream(selectFrom().where(predicate), page, o);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param page 查询参数
     * @param o 排序方式
     * @return Stream<T>
     */
    @Override
    public Stream<T> findStream(Page page, OrderSpecifier<?>... o){
        return fetchStream(selectFrom(), page, o);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param o 排序方式
     * @return Stream<T>
     */
    @Override
    public Stream<T> findStream(OrderSpecifier<?>... o){
        return fetchStream(selectFrom(), null, o);
    }

}
