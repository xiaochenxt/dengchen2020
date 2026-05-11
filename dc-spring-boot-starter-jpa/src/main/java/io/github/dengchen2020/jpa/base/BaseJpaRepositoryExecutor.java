package io.github.dengchen2020.jpa.base;

import io.github.dengchen2020.core.jdbc.BeforeInsertCallback;
import io.github.dengchen2020.core.jdbc.BeforeUpdateCallback;
import io.github.dengchen2020.core.utils.IterableUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import org.hibernate.jpa.AvailableHints;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * BaseJpaRepository的实现
 *
 * @author xiaochen
 * @since 2024/1/19
 */
@NullMarked
@Repository
@Transactional(propagation = Propagation.SUPPORTS)
public class BaseJpaRepositoryExecutor<T, ID> extends QuerydslJpaRepositoryExecutor<T, ID> implements
        QueryJpaRepository<T, ID>,
        EntityManagerRepository {

    protected final EntityManager entityManager;
    protected final JpaEntityInformation<T, ?> entityInformation;
    protected final PersistenceProvider provider;

    public BaseJpaRepositoryExecutor(JpaEntityInformation<T, ?> entityInformation, EntityManager em) {
        super(entityInformation, em);
        this.entityManager = em;
        this.entityInformation = entityInformation;
        this.provider = PersistenceProvider.fromEntityManager(em);
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

    @Override
    public <R> R execute(Function<EntityManager, R> function) {
        return function.apply(entityManager);
    }

    @Override
    public void detach(Object entity) {
        entityManager.detach(entity);
    }

    @Transactional
    @Override
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "Entity must not be null");

        if (entityInformation.isNew(entity)) {
            if (entity instanceof BeforeInsertCallback beforeInsertCallback) {
                beforeInsertCallback.beforeInsert();
            }
            entityManager.persist(entity);
            return entity;
        } else {
            if (entity instanceof BeforeUpdateCallback beforeUpdateCallback) {
                beforeUpdateCallback.beforeUpdate();
            }
            return entityManager.merge(entity);
        }
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

    @Transactional
    @Override
    public @Nullable T selectByIdForUpdateNowait(ID id) {
        var hints = getHints();
        hints.put(AvailableHints.HINT_SPEC_LOCK_TIMEOUT, 0);
        return entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_WRITE, hints);
    }

    @Transactional
    @Override
    public Optional<T> findByIdForUpdateNowait(ID id) {
        return Optional.ofNullable(selectByIdForUpdateNowait(id));
    }

    @Nullable
    @Override
    public T selectById(ID id) {
        CrudMethodMetadata metadata = super.getRepositoryMethodMetadata();
        if (metadata == null || metadata.getLockModeType() == null) {
            return entityManager.find(getDomainClass(), id, getHints());
        }
        return entityManager.find(getDomainClass(), id, metadata.getLockModeType(), getHints());
    }

    @Transactional
    @Override
    public @Nullable T selectByIdForShare(ID id) {
        return entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_READ, getHints());
    }

    @Transactional
    @Override
    public Optional<T> findByIdForShare(ID id) {
        return Optional.ofNullable(selectByIdForShare(id));
    }

    @Transactional
    @Override
    public @Nullable T selectByIdForShareNowait(ID id) {
        var hints = getHints();
        hints.put(AvailableHints.HINT_SPEC_LOCK_TIMEOUT, 0);
        return entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_READ, hints);
    }

    @Transactional
    @Override
    public Optional<T> findByIdForShareNowait(ID id) {
        return Optional.ofNullable(selectByIdForShareNowait(id));
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("不支持无条件的全量删除且该方法执行效率低");
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException("不支持无条件的全量删除，请自行实现");
    }

}
