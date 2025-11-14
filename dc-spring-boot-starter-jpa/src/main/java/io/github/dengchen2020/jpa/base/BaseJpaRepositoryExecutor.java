package io.github.dengchen2020.jpa.base;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.core.utils.IterableUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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
@NullMarked
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
    private static final String tenantIdSqlFragment = " and " + tenantIdFieldName + " = :tenantId";
    private static final String userIdSqlFragment = " and " + userIdFieldName + " = :userId";
    private static final String deletedFieldName = "deleted";

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
    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        for (S entity : entities) save(entity);
        return IterableUtils.toList(entities);
    }

    @Nullable
    @Transactional
    @Override
    public T selectByIdForUpdate(ID id) {
        return entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_WRITE, getHints());
    }

    @Transactional
    @Override
    public Optional<T> findByIdForUpdate(ID id) {
        return Optional.ofNullable(selectByIdForUpdate(id));
    }

    @Nullable
    @Override
    public T selectById(ID id) {
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

    @Override
    public int softDelete(Iterable<ID> ids) {
        Query query = entityManager.createQuery(softDeleteInSql)
                .setParameter(deletedFieldName, true)
                .setParameter(idFieldName, ids);
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
        Query query = entityManager.createQuery(softDeleteSql)
                .setParameter(deletedFieldName, true)
                .setParameter(idFieldName, id);
        return query.executeUpdate();
    }

    private Long getTenantIdNonNull(){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        Assert.notNull(authentication, "未设置authentication，无法获取tenantId");
        Long tenantId = authentication.tenantId();
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
        if (authentication == null || authentication.tenantId() == null) return null;
        return authentication.tenantId();
    }

    @Nullable
    private String getUserId(){
        if (strictVerifyAuthentication) return getUserIdNonNull();
        Authentication authentication = SecurityContextHolder.getAuthentication();
        if (authentication == null || authentication.userId() == null) return null;
        return authentication.userId();
    }

    @Nullable
    @Override
    public T selectByIdWithTenantId(ID id) {
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
    public List<T> selectInIdsWithTenantId(Iterable<ID> ids) {
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null ){
            query = entityManager.createQuery(selectInSql + tenantIdSqlFragment, getDomainClass())
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
    public final List<T> selectInIdsWithTenantId(ID... ids) {
        Assert.notEmpty(ids, "ids must not be null");
        return selectInIdsWithTenantId(List.of(ids));
    }

    @Override
    public Optional<T> findByIdWithTenantId(ID id) {
        return Optional.ofNullable(selectByIdWithTenantId(id));
    }

    @Override
    public int deleteWithTenantId(ID id) {
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null){
            query = entityManager.createQuery(deleteSql + tenantIdSqlFragment)
                    .setParameter(idFieldName, id)
                    .setParameter(tenantIdFieldName, tenantId);
        }else {
            query = entityManager.createQuery(deleteSql)
                    .setParameter(idFieldName, id);
        }
        return query.executeUpdate();
    }

    @Override
    public int deleteWithTenantId(Iterable<ID> ids) {
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null) {
            query = entityManager.createQuery(deleteInSql + tenantIdSqlFragment)
                    .setParameter(idFieldName, ids)
                    .setParameter(tenantIdFieldName, tenantId);
        }else {
            query = entityManager.createQuery(deleteInSql)
                    .setParameter(idFieldName, ids);
        }
        return query.executeUpdate();
    }

    @SafeVarargs
    @Override
    public final int deleteWithTenantId(ID... ids) {
        return deleteWithTenantId(List.of(ids));
    }

    @Override
    public int softDeleteWithTenantId(ID id) {
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null){
            query = entityManager.createQuery(softDeleteSql + tenantIdSqlFragment)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, id)
                    .setParameter(tenantIdFieldName, tenantId);
        }else {
            query = entityManager.createQuery(softDeleteSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, id);
        }
        return query.executeUpdate();
    }

    @Override
    public int softDeleteWithTenantId(Iterable<ID> ids) {
        Long tenantId = getTenantId();
        Query query;
        if (strictVerifyAuthentication || tenantId != null){
            query = entityManager.createQuery(softDeleteInSql + tenantIdSqlFragment)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, ids)
                    .setParameter(tenantIdFieldName, tenantId);
        }else {
            query = entityManager.createQuery(softDeleteInSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, ids);
        }
        return query.executeUpdate();
    }

    @SafeVarargs
    @Override
    public final int softDeleteWithTenantId(ID... ids) {
        return softDeleteWithTenantId(List.of(ids));
    }

    @Nullable
    @Override
    public T selectByIdWithUserId(ID id) {
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
    public List<T> selectInIdsWithUserId(Iterable<ID> ids) {
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(selectInSql + userIdSqlFragment, getDomainClass())
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
    public final List<T> selectInIdsWithUserId(ID... ids) {
        Assert.notEmpty(ids, "ids must not be null");
        return selectInIdsWithUserId(List.of(ids));
    }

    @Override
    public Optional<T> findByIdWithUserId(ID id) {
        return Optional.ofNullable(selectByIdWithUserId(id));
    }

    @Override
    public int deleteWithUserId(ID id) {
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(deleteSql + userIdSqlFragment)
                    .setParameter(idFieldName, id)
                    .setParameter(userIdFieldName, userId);
        }else {
            query = entityManager.createQuery(deleteSql)
                    .setParameter(idFieldName, id);
        }
        return query.executeUpdate();
    }

    @Override
    public int deleteWithUserId(Iterable<ID> ids) {
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(deleteInSql + userIdSqlFragment)
                    .setParameter(idFieldName, ids)
                    .setParameter(userIdFieldName, userId);
        }else {
            query = entityManager.createQuery(deleteInSql)
                    .setParameter(idFieldName, ids);
        }
        return query.executeUpdate();
    }

    @SafeVarargs
    @Override
    public final int deleteWithUserId(ID... ids) {
        return deleteWithUserId(List.of(ids));
    }

    @Override
    public int softDeleteWithUserId(ID id) {
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(softDeleteSql + userIdSqlFragment)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, id)
                    .setParameter(userIdFieldName, userId);
        }else {
            query = entityManager.createQuery(softDeleteSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, id);
        }
        return query.executeUpdate();
    }

    @Override
    public int softDeleteWithUserId(Iterable<ID> ids) {
        String userId = getUserId();
        Query query;
        if (strictVerifyAuthentication || userId != null){
            query = entityManager.createQuery(softDeleteInSql + userIdSqlFragment)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, ids)
                    .setParameter(userIdFieldName, userId);
        }else {
            query = entityManager.createQuery(softDeleteInSql)
                    .setParameter(deletedFieldName, true)
                    .setParameter(idFieldName, ids);
        }
        return query.executeUpdate();
    }

    @SafeVarargs
    @Override
    public final int softDeleteWithUserId(ID... ids) {
        return softDeleteWithUserId(List.of(ids));
    }

}
