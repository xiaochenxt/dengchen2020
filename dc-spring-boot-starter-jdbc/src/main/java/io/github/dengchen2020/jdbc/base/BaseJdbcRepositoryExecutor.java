package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLUpdateClause;
import io.github.dengchen2020.core.jdbc.Page;
import io.github.dengchen2020.core.jdbc.SimplePage;
import io.github.dengchen2020.core.jdbc.TenantQuery;
import io.github.dengchen2020.core.jdbc.UserQuery;
import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.core.security.principal.TenantInfo;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jdbc.core.JdbcAggregateOperations;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.dengchen2020.jdbc.base.CacheSupport.entityPathMap;
import static io.github.dengchen2020.jdbc.base.CacheSupport.pathBuilderMap;

/**
 * JDBC操作通用接口实现
 * @author xiaochen
 * @since 2025/12/8
 */
@NullMarked
@Transactional(propagation = Propagation.SUPPORTS)
public class BaseJdbcRepositoryExecutor<T,ID> implements BaseJdbcRepository<T, ID>, JdbcRepositoryExtension<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(BaseJdbcRepositoryExecutor.class);

    private final JdbcAggregateOperations operations;
    private final SQLQueryFactory queryFactory;

    private static final String idFieldName = "id";
    private static final String userIdFieldName = "userId";
    private static final String deletedFieldName = "deleted";
    private static final String tenantIdFieldName = "tenantId";
    private static final boolean strictVerifyAuthentication = false;

    public BaseJdbcRepositoryExecutor(JdbcAggregateOperations operations, SQLQueryFactory queryFactory) {
        this.operations = operations;
        if (operations instanceof JdbcAggregateTemplate jdbcAggregateTemplate) {
            jdbcAggregateTemplate.setEntityLifecycleEventsEnabled(false);
        }
        this.queryFactory = queryFactory;
    }

    @SuppressWarnings("unchecked")
    protected RelationalPathBase<T> entityPath() {
        return (RelationalPathBase<T>) entityPathMap.computeIfAbsent(getDomainClass(), SimpleEntityPathResolver.INSTANCE::createPath);
    }

    @SuppressWarnings("unchecked")
    private RelationalPathBase<T> entityPath(Class<T> domainClass) {
        return (RelationalPathBase<T>) entityPathMap.computeIfAbsent(domainClass, SimpleEntityPathResolver.INSTANCE::createPath);
    }

    @SuppressWarnings("unchecked")
    public PathBuilder<T> builder() {
        return (PathBuilder<T>) pathBuilderMap.computeIfAbsent(getDomainClass(), (k) -> {
            var entityPath = entityPath((Class<T>) k);
            return new PathBuilder<>(entityPath.getType(), entityPath.getMetadata());
        });
    }

    public EntityPath<T> path() {
        return entityPath();
    }

    public SQLQuery<?> query() {
        return queryFactory.query().from(entityPath());
    }

    @Override
    public <R> SQLQuery<R> select(Expression<R> expr) {
        return queryFactory.select(expr).from(entityPath());
    }

    @Override
    public SQLQuery<Tuple> select(Expression<?>... exprs) {
        return queryFactory.select(exprs).from(entityPath());
    }

    @Override
    public <R> SQLQuery<R> selectDistinct(Expression<R> expr) {
        return queryFactory.selectDistinct(expr).from(entityPath());
    }

    @Override
    public SQLQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return queryFactory.selectDistinct(exprs).from(entityPath());
    }

    @Override
    public SQLQuery<Integer> selectOne() {
        return queryFactory.selectOne().from(entityPath());
    }

    @Override
    public SQLQuery<Integer> selectZero() {
        return queryFactory.selectZero().from(entityPath());
    }

    @Override
    public SQLQuery<T> selectFrom() {
        var domainClass = getDomainClass();
        var entityPath = entityPath(domainClass);
        return queryFactory.select(Projections.bean(domainClass, entityPath.all())).from(entityPath);
    }

    @Override
    public SQLUpdateClause update(Predicate[] where) {
        Assert.notEmpty(where, "更新必须有条件");
        return queryFactory.update(entityPath()).where(where);
    }

    @Override
    public long delete(Predicate[] where) {
        Assert.notEmpty(where, "删除必须有条件");
        return queryFactory.delete(entityPath()).where(where).execute();
    }

    @Override
    public <R> SimplePage<R> fetchPage(SQLQuery<R> query, Page page, OrderSpecifier<?>... o) {
        if (page.getSize() == 0)
            return new SimplePage<>(!page.isSelectCount() ? 0 : query.fetchCount(), Collections.emptyList());
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
    public SimplePage<T> findAll(Predicate predicate, Page page, OrderSpecifier<?>... o) {
        return fetchPage(selectFrom().where(predicate), page, o);
    }

    @Override
    public <R> Stream<R> fetchStream(SQLQuery<R> query, @Nullable Page page, OrderSpecifier<?>... o) {
        if (o.length > 0) query = query.orderBy(o);
        if (page == null) return query.stream();
        return query.limit(page.getSize())
                .offset(page.getOffset())
                .stream();
    }

    @Override
    public Stream<T> findStream(Predicate predicate, @Nullable Page page, OrderSpecifier<?>... o) {
        return fetchStream(selectFrom().where(predicate), page, o);
    }

    @Override
    public Stream<T> findStream(@Nullable Page page, OrderSpecifier<?>... o) {
        return fetchStream(selectFrom(), page, o);
    }

    @Override
    public Stream<T> findStream(OrderSpecifier<?>... o) {
        return fetchStream(selectFrom(), null, o);
    }

    @Transactional
    @Override
    public @Nullable T selectByIdForUpdate(ID id) {
        return selectFrom().where(builder().get(idFieldName).eq(id)).forUpdate().fetchFirst();
    }

    @Transactional
    @Override
    public Optional<T> findByIdForUpdate(ID id) {
        return Optional.ofNullable(selectByIdForUpdate(id));
    }

    @Override
    public Optional<T> findById(@NonNull ID id) {
        return Optional.ofNullable(selectById(id));
    }

    @Override
    public @Nullable T selectById(ID id) {
        return operations.findById(id, getDomainClass());
    }

    @Transactional
    @Override
    public @Nullable T selectByIdForShare(ID id) {
        return selectFrom().where(builder().get(idFieldName).eq(id)).forUpdate().fetchFirst();
    }

    @Transactional
    @Override
    public Optional<T> findByIdForShare(ID id) {
        return Optional.ofNullable(selectByIdForShare(id));
    }

    @NonNull
    @Transactional
    @Override
    public <S extends T> S save(@NonNull S entity) {
        return operations.save(entity);
    }

    @Override
    public boolean existsById(@NonNull ID id) {
        return operations.existsById(id, getDomainClass());
    }

    @Override
    public long count() {
        return operations.count(getDomainClass());
    }

    @Transactional
    @Override
    public void deleteById(@NonNull ID id) {
        operations.deleteById(id, getDomainClass());
    }

    @Transactional
    @Override
    public void delete(@NonNull T entity) {
        operations.delete(entity);
    }

    @Transactional
    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        operations.deleteAllById(ids, getDomainClass());
    }

    @Transactional
    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        operations.deleteAll(entities);
    }

    @Transactional
    @Override
    public void deleteAll() {
        operations.deleteAll(getDomainClass());
    }

    @Transactional
    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        return operations.saveAll(entities);
    }

    @Override
    public List<T> findAll() {
        return operations.findAll(getDomainClass());
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        return operations.findAllById(ids, getDomainClass());
    }

    @Override
    public List<T> findAll(Sort sort) {
        return operations.findAll(getDomainClass(), sort);
    }

    @Override
    public org.springframework.data.domain.Page<T> findAll(Pageable pageable) {
        Assert.notNull(pageable, "Pageable must not be null");

        Query query = Query.query(CriteriaDefinition.empty()).with(pageable);
        var domainClass = getDomainClass();
        List<T> content = operations.findAll(query, domainClass);

        return PageableExecutionUtils.getPage(content, pageable, () -> operations.count(domainClass));
    }

    @Override
    public long softDelete(Iterable<ID> ids) {
        var builder = builder();
        return update(builder.get(idFieldName).in(ids)).set(builder.get(deletedFieldName), true).execute();
    }

    @Override
    public long softDelete(ID... ids) {
        var builder = builder();
        return update(builder.get(idFieldName).in(ids)).set(builder.get(deletedFieldName), true).execute();
    }

    @Override
    public long softDelete(ID id) {
        var builder = builder();
        return update(builder.get(idFieldName).eq(id)).set(builder.get(deletedFieldName), true).execute();
    }

    private Long getTenantIdNonNull(){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        Assert.notNull(authentication, "未设置authentication，无法获取tenantId");
        if (!(authentication instanceof TenantInfo tenantInfo)) {
            throw new IllegalArgumentException(authentication.getName() + "未实现 " + TenantInfo.class.getName());
        }
        Long tenantId = tenantInfo.tenantId();
        Assert.notNull(tenantId, "tenantId must not be null");
        return tenantId;
    }

    private String getUserIdNonNull(){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        Assert.notNull(authentication, "未设置authentication，无法获取userId");
        String userId = authentication.userId();
        Assert.notNull(userId, "userId must not be null");
        return userId;
    }

    @Nullable
    private Long getTenantId(){
        if (strictVerifyAuthentication) return getTenantIdNonNull();
        Authentication authentication = SecurityContextHolder.getAuthentication();
        if (!(authentication instanceof TenantInfo tenantInfo)) return null;
        return tenantInfo.tenantId();
    }

    @Nullable
    private String getUserId(){
        if (strictVerifyAuthentication) return getUserIdNonNull();
        Authentication authentication = SecurityContextHolder.getAuthentication();
        if (authentication == null || authentication.userId() == null) return null;
        return authentication.userId();
    }

    @Override
    public @Nullable T selectByIdWithUserId(ID id) {
        String userId = getUserId();
        T t = selectById(id);
        if (t == null) return null;
        if (userId == null) return t;
        if (t instanceof UserQuery<?> userQuery) {
            Object dataUserId = userQuery.getUserId();
            if (dataUserId == null){
                if (log.isWarnEnabled()) log.warn("id：{}，{}.getUserId()返回null，条件无法携带userId", id, getDomainClass());
            }else {
                if (!dataUserId.toString().equals(userId)) return null;
            }
            return t;
        }
        Method getUserIdMethod = ReflectionUtils.findMethod(getDomainClass(), "getUserId");
        if (getUserIdMethod == null) {
            if (log.isWarnEnabled()) log.warn("{}未找到getUserId()，条件无法携带userId", getDomainClass());
        }else {
            ReflectionUtils.makeAccessible(getUserIdMethod);
            Object dataUserId = ReflectionUtils.invokeMethod(getUserIdMethod, t);
            if (dataUserId == null){
                if (log.isWarnEnabled()) log.warn("id：{}，{}.getUserId()返回null，条件无法携带userId", id, getDomainClass());
            }else {
                if (!dataUserId.toString().equals(userId)) return null;
            }
        }
        return t;
    }

    @Override
    public List<T> selectInIdsWithUserId(Iterable<ID> ids) {
        String userId = getUserId();
        var builder = builder();
        if (strictVerifyAuthentication || userId != null ){
            return selectFrom().where(builder.get(idFieldName).in(ids).and(builder.get(userIdFieldName).eq(userId))).fetch();
        } else {
            return selectFrom().where(builder.get(idFieldName).in(ids)).fetch();
        }
    }

    @Override
    public List<T> selectInIdsWithUserId(ID... ids) {
        return selectInIdsWithUserId(List.of(ids));
    }

    @Override
    public Optional<T> findByIdWithUserId(ID id) {
        return Optional.ofNullable(selectByIdWithUserId(id));
    }

    @Override
    public long deleteWithUserId(ID id) {
        String userId = getUserId();
        var builder = builder();
        if (strictVerifyAuthentication || userId != null){
            return delete(builder.get(idFieldName).eq(id).and(builder.get(userIdFieldName).eq(userId)));
        } else {
            return delete(builder.get(idFieldName).eq(id));
        }
    }

    @Override
    public long deleteWithUserId(Iterable<ID> ids) {
        String userId = getUserId();
        var builder = builder();
        if (strictVerifyAuthentication || userId != null){
            return delete(builder.get(idFieldName).in(ids).and(builder.get(userIdFieldName).eq(userId)));
        } else {
            return delete(builder.get(idFieldName).in(ids));
        }
    }

    @Override
    public long deleteWithUserId(ID... ids) {
        return deleteWithUserId(List.of(ids));
    }

    @Override
    public long softDeleteWithUserId(Iterable<ID> ids) {
        String userId = getUserId();
        var builder = builder();
        if (strictVerifyAuthentication || userId != null){
            return update(builder.get(idFieldName).in(ids).and(builder.get(userIdFieldName).eq(userId))).set(builder.get(deletedFieldName), true).execute();
        } else {
            return update(builder.get(idFieldName).in(ids)).set(builder.get(deletedFieldName), true).execute();
        }
    }

    @Override
    public long softDeleteWithUserId(ID... ids) {
        return softDeleteWithUserId(List.of(ids));
    }

    @Override
    public long softDeleteWithUserId(ID id) {
        String userId = getUserId();
        var builder = builder();
        if (strictVerifyAuthentication || userId != null){
            return update(builder.get(idFieldName).eq(id).and(builder.get(userIdFieldName).eq(userId))).set(builder.get(deletedFieldName), true).execute();
        } else {
            return update(builder.get(idFieldName).eq(id)).set(builder.get(deletedFieldName), true).execute();
        }
    }

    @Override
    public @Nullable T selectByIdWithTenantId(ID id) {
        Long tenantId = getTenantId();
        var t = selectById(id);
        if (t == null) return null;
        if (tenantId == null) return t;
        if (t instanceof TenantQuery tenantQuery) {
            Long dataTenantId = tenantQuery.getTenantId();
            if (dataTenantId == null){
                if (log.isWarnEnabled()) log.warn("id：{}，{}.getTenantId()返回null，条件无法携带tenantId", id, getDomainClass());
            }else {
                if (!dataTenantId.equals(tenantId)) return null;
            }
            return t;
        }
        Method getTenantIdMethod = ReflectionUtils.findMethod(getDomainClass(), "getTenantId");
        if (getTenantIdMethod == null) {
            if (log.isWarnEnabled()) log.warn("{}未找到getTenantId()，条件无法携带tenantId", getDomainClass());
        }else {
            ReflectionUtils.makeAccessible(getTenantIdMethod);
            Long dataTenantId = (Long) ReflectionUtils.invokeMethod(getTenantIdMethod, t);
            if (dataTenantId == null){
                if (log.isWarnEnabled()) log.warn("id：{}，{}.getTenantId()返回null，条件无法携带tenantId", id, getDomainClass());
            }else {
                if (!dataTenantId.equals(tenantId)) return null;
            }
        }
        return t;
    }

    @Override
    public List<T> selectInIdsWithTenantId(Iterable<ID> ids) {
        Long tenantId = getTenantId();
        var builder = builder();
        if (strictVerifyAuthentication || tenantId != null ){
            return selectFrom().where(builder.get(idFieldName).in(ids).and(builder.get(tenantIdFieldName).eq(tenantId))).fetch();
        } else {
            return selectFrom().where(builder.get(idFieldName).in(ids)).fetch();
        }
    }

    @Override
    public List<T> selectInIdsWithTenantId(ID... ids) {
        return selectInIdsWithTenantId(List.of(ids));
    }

    @Override
    public Optional<T> findByIdWithTenantId(ID id) {
        return Optional.ofNullable(selectByIdWithTenantId(id));
    }

    @Override
    public long deleteWithTenantId(ID id) {
        Long tenantId = getTenantId();
        var builder = builder();
        if (strictVerifyAuthentication || tenantId != null){
            return delete(builder.get(idFieldName).eq(id).and(builder.get(tenantIdFieldName).eq(tenantId)));
        } else {
            return delete(builder.get(idFieldName).eq(id));
        }
    }

    @Override
    public long deleteWithTenantId(Iterable<ID> ids) {
        Long tenantId = getTenantId();
        var builder = builder();
        if (strictVerifyAuthentication || tenantId != null){
            return delete(builder.get(idFieldName).in(ids).and(builder.get(tenantIdFieldName).eq(tenantId)));
        } else {
            return delete(builder.get(idFieldName).in(ids));
        }
    }

    @Override
    public long deleteWithTenantId(ID... ids) {
        return deleteWithTenantId(List.of(ids));
    }

    @Override
    public long softDeleteWithTenantId(Iterable<ID> ids) {
        Long tenantId = getTenantId();
        var builder = builder();
        if (strictVerifyAuthentication || tenantId != null){
            return update(builder.get(idFieldName).in(ids).and(builder.get(tenantIdFieldName).eq(tenantId))).set(builder.get(deletedFieldName), true).execute();
        } else {
            return update(builder.get(idFieldName).in(ids)).set(builder.get(deletedFieldName), true).execute();
        }
    }

    @Override
    public long softDeleteWithTenantId(ID... ids) {
        return softDeleteWithTenantId(List.of(ids));
    }

    @Override
    public long softDeleteWithTenantId(ID id) {
        Long tenantId = getTenantId();
        var builder = builder();
        if (strictVerifyAuthentication || tenantId != null){
            return update(builder.get(idFieldName).eq(id).and(builder.get(tenantIdFieldName).eq(tenantId))).set(builder.get(deletedFieldName), true).execute();
        } else {
            return update(builder.get(idFieldName).eq(id)).set(builder.get(deletedFieldName), true).execute();
        }
    }

    @Override
    public Optional<T> findOne(Predicate predicate) {
        return Optional.ofNullable(selectFrom().where(predicate).fetchFirst());
    }

    @Override
    public List<T> findAll(Predicate predicate) {
        return selectFrom().where(predicate).fetch();
    }

    @Override
    public List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders) {
        return selectFrom().where(predicate).orderBy(orders).fetch();
    }

    @Override
    public List<T> findAll(OrderSpecifier<?>... orders) {
        return selectFrom().orderBy(orders).fetch();
    }

    @Override
    public long count(Predicate predicate) {
        return selectOne().where(predicate).fetchCount();
    }

    @Override
    public boolean exists(Predicate predicate) {
        return selectOne().where(predicate).fetchFirst() != null;
    }

    @Override
    public long delete(Iterable<ID> ids) {
        return delete(builder().get(idFieldName).in(ids));
    }

    @Override
    public long delete(ID... ids) {
        return delete(List.of(ids));
    }

    @Override
    public List<T> selectInIds(Iterable<ID> ids) {
        return findAllById(ids);
    }

    @Override
    public List<T> selectInIds(ID... ids) {
        return selectInIds(List.of(ids));
    }

    @Override
    public boolean exists(ID id) {
        return existsById(id);
    }
}
