package io.github.dengchen2020.jpa.base;

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
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * BaseJpaRepository的实现
 *
 * @author xiaochen
 * @since 2024/1/19
 */
@NullMarked
@Transactional(propagation = Propagation.SUPPORTS)
public class BaseJpaRepositoryExecutor<T, ID> extends SimpleJpaRepository<T, ID> implements
        QueryJpaRepository<T, ID>, EntityManagerRepository<T> {

    private final EntityManager entityManager;
    private final JpaEntityInformation<T, ?> entityInformation;
    private final PersistenceProvider provider;

    public BaseJpaRepositoryExecutor(JpaEntityInformation<T, ?> entityInformation, final EntityManager em) {
        super(entityInformation, em);
        this.entityManager = em;
        this.entityInformation = entityInformation;
        this.provider = PersistenceProvider.fromEntityManager(em);
    }

    private Map<String, Object> getHints() {
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

    private void applyQueryHintsForCount(Query query) {
        CrudMethodMetadata metadata = super.getRepositoryMethodMetadata();
        if (metadata == null) return;
        getQueryHintsForCount().forEach(query::setHint);
        applyComment(metadata, query::setHint);
    }

    @Override
    public void clear() {
        entityManager.clear();
    }

    @Override
    public void detach(T entity) {
        entityManager.detach(entity);
    }

    @Transactional
    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        for (S entity : entities) save(entity);
        return IterableUtils.toList(entities);
    }

    @Nullable
    @Transactional(readOnly = true)
    @Override
    public T selectByIdForUpdate(ID id) {
        return entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_WRITE, getHints());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<T> findByIdForUpdate(ID id) {
        return Optional.ofNullable(selectByIdForUpdate(id));
    }

    @Transactional(readOnly = true)
    @Override
    public @Nullable T selectByIdForUpdateNowait(ID id) {
        var hints = getHints();
        hints.put(AvailableHints.HINT_SPEC_LOCK_TIMEOUT, 0);
        return entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_WRITE, hints);
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    @Override
    public @Nullable T selectByIdForShare(ID id) {
        return entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_READ, getHints());
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<T> findByIdForShare(ID id) {
        return Optional.ofNullable(selectByIdForShare(id));
    }

    @Transactional(readOnly = true)
    @Override
    public @Nullable T selectByIdForShareNowait(ID id) {
        var hints = getHints();
        hints.put(AvailableHints.HINT_SPEC_LOCK_TIMEOUT, 0);
        return entityManager.find(getDomainClass(), id, LockModeType.PESSIMISTIC_READ, hints);
    }

    @Transactional(readOnly = true)
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
