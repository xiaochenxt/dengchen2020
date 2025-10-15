package io.github.dengchen2020.jpa.base;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.core.utils.IterableUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * BaseJpaRepository的实现
 *
 * @author xiaochen
 * @since 2024/1/19
 */
@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class BaseJpaRepositoryExecutor<T, ID> extends QuerydslRepositoryExecutor<T, ID> implements
        QueryJpaRepository<T, ID>, SoftDeleteRepository<T, ID>, TenantJpaRepository<T, ID>, UserIdJpaRepository<T, ID> {

    private static final Logger log = LoggerFactory.getLogger(BaseJpaRepositoryExecutor.class);

    final String idFieldName;
    static final String tenantIdFieldName = "tenantId";
    static final String userIdFieldName = "userId";

    private static final boolean strictVerifyAuthentication = false;
    private final String selectInSql;
    private final String deleteSql;
    private final String deleteInSql;
    private final String softDeleteSql;
    private final String softDeleteInSql;
    private static final String tenantIdSql = " and " + tenantIdFieldName + " = :tenantId";
    private static final String userIdSql = " and " + userIdFieldName + " = :userId";

    public BaseJpaRepositoryExecutor(JpaEntityInformation<T, ?> entityInformation, final EntityManager em, final JPAQueryFactory queryFactory) {
        super(entityInformation, em, queryFactory);
        this.idFieldName = entity.getIdAttribute() == null ? "id" : entity.getIdAttribute().getName();
        this.selectInSql = "from " + entity.getEntityName() + " where "+idFieldName+" in :id";
        this.deleteSql = "delete from " + entity.getEntityName() + " where "+idFieldName+" = :id";
        this.deleteInSql = "delete from " + entity.getEntityName() + " where "+idFieldName+" in :id";
        this.softDeleteSql = "update " + entity.getEntityName() + " set "+deletedFieldName+" = :deleted where " + idFieldName + " = :id";
        this.softDeleteInSql = "update " + entity.getEntityName() + " set "+deletedFieldName+" = :deleted where " + idFieldName + " in :id";
    }

    @Transactional
    @Nonnull
    @Override
    public <S extends T> List<S> saveAll(@Nonnull Iterable<S> entities) {
        Assert.notNull(entities, "entities must not be null");
        for (S entity : entities) save(entity);
        return IterableUtils.toList(entities);
    }

    /**
     * selectById 加锁版本
     *
     * @param id id
     * @return T
     */
    @Transactional
    @Override
    public T selectByIdForUpdate(@Nonnull ID id) {
        Assert.notNull(id, "id must not be null");
        return entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_WRITE, getHints());
    }

    /**
     * findById 加锁版本
     *
     * @param id id
     * @return Optional<T>
     */
    @Transactional
    @Nonnull
    @Override
    public Optional<T> findByIdForUpdate(@Nonnull ID id) {
        return Optional.ofNullable(selectByIdForUpdate(id));
    }

    /**
     * 根据id查询
     * @param id id
     * @return Optional<T>
     */
    @Override
    public T selectById(@Nonnull ID id) {
        Assert.notNull(id, "id must not be null");
        CrudMethodMetadata metadata = super.getRepositoryMethodMetadata();
        if (metadata == null) {
            return entityManager.find(getDomainClass(), id, getHints());
        }
        return entityManager.find(getDomainClass(), id, metadata.getLockModeType(), getHints());
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("不支持无条件的全量删除且该方法执行效率低");
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException("不支持无条件的全量删除，请自行实现");
    }

    /**
     * 批量逻辑删除
     * @param ids id集合
     * @return 受影响的条数
     */
    @Override
    public int softDelete(Iterable<ID> ids) {
        Assert.notNull(ids, "ids must not be null");
        Query query = entityManager.createQuery(softDeleteInSql)
                .setParameter(deletedFieldName, true)
                .setParameter(idFieldName, ids);
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @SafeVarargs
    @Override
    public final int softDelete(ID... ids) {
        return softDelete(List.of(ids));
    }

    /**
     * 逻辑删除
     * @param id id
     * @return 受影响的条数
     */
    @Override
    public int softDelete(ID id) {
        Assert.notNull(id, "id must not be null");
        Query query = entityManager.createQuery(softDeleteSql)
                .setParameter(deletedFieldName, true)
                .setParameter(idFieldName, id);
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @Nonnull
    private Long getTenantIdNonNull(){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        Assert.notNull(authentication, "未设置authentication，无法获取tenantId");
        Long tenantId = authentication.getTenantId();
        Assert.notNull(tenantId, "tenantId must not be null");
        return tenantId;
    }

    @Nonnull
    private String getUserIdNonNull(){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        Assert.notNull(authentication, "未设置authentication，无法获取userId");
        String userId = authentication.getUserId();
        Assert.notNull(userId, "userId must not be null");
        return userId;
    }

    @Nullable
    private Long getTenantId(){
        if (strictVerifyAuthentication) return getTenantIdNonNull();
        Authentication authentication = SecurityContextHolder.getAuthentication();
        if (authentication == null || authentication.getUserId() == null) return null;
        return authentication.getTenantId();
    }

    @Nullable
    private String getUserId(){
        if (strictVerifyAuthentication) return getUserIdNonNull();
        Authentication authentication = SecurityContextHolder.getAuthentication();
        if (authentication == null || authentication.getUserId() == null) return null;
        return authentication.getUserId();
    }

    /**
     * 根据id查询（携带租户id）
     * @param id id
     * @return T
     */
    @Override
    public T selectByIdWithTenantId(@Nonnull ID id) {
        Long tenantId = getTenantId();
        T t = selectById(id);
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

    @SuppressWarnings("unchecked")
    @Override
    public List<T> selectInIdsWithTenantId(@Nonnull Iterable<ID> ids) {
        Assert.notNull(ids, "ids must not be null");
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null ){
            query = entityManager.createQuery(selectInSql + tenantIdSql, getDomainClass())
                    .setParameter(idFieldName, ids)
                    .setParameter(tenantIdFieldName, tenantId);
        }else {
            query = entityManager.createQuery(selectInSql, getDomainClass())
                    .setParameter(idFieldName, ids);
        }
        getHints().forEach(query::setHint);
        return query.getResultList();
    }

    @SafeVarargs
    @Override
    public final List<T> selectInIdsWithTenantId(@Nonnull ID... ids) {
        Assert.notEmpty(ids, "ids must not be null");
        return selectInIdsWithTenantId(List.of(ids));
    }

    @Override
    public Optional<T> findByIdWithTenantId(@Nonnull ID id) {
        return Optional.ofNullable(selectByIdWithTenantId(id));
    }

    @Override
    public int deleteWithTenantId(@Nonnull ID id) {
        Assert.notNull(id, "id must not be null");
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null){
            query = entityManager.createQuery(deleteSql + tenantIdSql)
                    .setParameter(idFieldName, id)
                    .setParameter(tenantIdFieldName, tenantId);
        }else {
            query = entityManager.createQuery(deleteSql)
                    .setParameter(idFieldName, id);
        }
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @Override
    public int deleteWithTenantId(@Nonnull Iterable<ID> ids) {
        Assert.notNull(ids, "ids must not be null");
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null) {
            query = entityManager.createQuery(deleteInSql + tenantIdSql)
                    .setParameter(idFieldName, ids)
                    .setParameter(tenantIdFieldName, tenantId);
        }else {
            query = entityManager.createQuery(deleteInSql)
                    .setParameter(idFieldName, ids);
        }
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @SafeVarargs
    @Override
    public final int deleteWithTenantId(@Nonnull ID... ids) {
        return deleteWithTenantId(List.of(ids));
    }

    @Override
    public int softDeleteWithTenantId(@Nonnull ID id) {
        Assert.notNull(id, "ids must not be null");
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null){
            query = entityManager.createQuery(softDeleteSql + tenantIdSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, id)
                    .setParameter(tenantIdFieldName, tenantId);
        }else {
            query = entityManager.createQuery(softDeleteSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, id);
        }
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @Override
    public int softDeleteWithTenantId(@Nonnull Iterable<ID> ids) {
        Assert.notNull(ids, "ids must not be null");
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null){
            query = entityManager.createQuery(softDeleteInSql + tenantIdSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, ids)
                    .setParameter(tenantIdFieldName, tenantId);
        }else {
            query = entityManager.createQuery(softDeleteInSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, ids);
        }
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @SafeVarargs
    @Override
    public final int softDeleteWithTenantId(@Nonnull ID... ids) {
        return softDeleteWithTenantId(List.of(ids));
    }

    /**
     * 根据id查询（携带用户id）
     * @param id id
     * @return T
     */
    @Override
    public T selectByIdWithUserId(@Nonnull ID id) {
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

    @SuppressWarnings("unchecked")
    @Override
    public List<T> selectInIdsWithUserId(@Nonnull Iterable<ID> ids) {
        Assert.notNull(ids, "ids must not be null");
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(selectInSql + userIdSql, getDomainClass())
                    .setParameter(idFieldName, ids)
                    .setParameter(userIdFieldName, userId);
        }else {
            query = entityManager.createQuery(selectInSql)
                    .setParameter(idFieldName, ids);
        }
        getHints().forEach(query::setHint);
        return query.getResultList();
    }

    @SafeVarargs
    @Override
    public final List<T> selectInIdsWithUserId(@Nonnull ID... ids) {
        Assert.notEmpty(ids, "ids must not be null");
        return selectInIdsWithUserId(List.of(ids));
    }

    @Override
    public Optional<T> findByIdWithUserId(@Nonnull ID id) {
        return Optional.ofNullable(selectByIdWithUserId(id));
    }

    @Override
    public int deleteWithUserId(@Nonnull ID id) {
        Assert.notNull(id, "id must not be null");
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(deleteSql +userIdSql)
                    .setParameter(idFieldName, id)
                    .setParameter(userIdFieldName, userId);
        }else {
            query = entityManager.createQuery(deleteSql)
                    .setParameter(idFieldName, id);
        }
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @Override
    public int deleteWithUserId(@Nonnull Iterable<ID> ids) {
        Assert.notNull(ids, "ids must not be null");
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(deleteInSql + userIdSql)
                    .setParameter(idFieldName, ids)
                    .setParameter(userIdFieldName, userId);
        }else {
            query = entityManager.createQuery(deleteInSql)
                    .setParameter(idFieldName, ids);
        }
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @SafeVarargs
    @Override
    public final int deleteWithUserId(@Nonnull ID... ids) {
        return deleteWithUserId(List.of(ids));
    }

    @Override
    public int softDeleteWithUserId(@Nonnull ID id) {
        Assert.notNull(id, "id must not be null");
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(softDeleteSql + userIdSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, id)
                    .setParameter(userIdFieldName, userId);
        }else {
            query = entityManager.createQuery(softDeleteSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, id);
        }
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @Override
    public int softDeleteWithUserId(@Nonnull Iterable<ID> ids) {
        Assert.notNull(ids, "ids must not be null");
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(softDeleteInSql + userIdSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, ids)
                    .setParameter(userIdFieldName, userId);
        }else {
            query = entityManager.createQuery(softDeleteInSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, ids);
        }
        getHints().forEach(query::setHint);
        return query.executeUpdate();
    }

    @SafeVarargs
    @Override
    public final int softDeleteWithUserId(@Nonnull ID... ids) {
        return softDeleteWithUserId(List.of(ids));
    }

}
