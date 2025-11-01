package io.github.dengchen2020.jpa.base;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 简单的Jpa查询接口
 * @author xiaochen
 * @since 2024/8/1
 */
@NullMarked
@NoRepositoryBean
public interface QueryJpaRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * selectById 加锁版本
     *
     * @param id id
     * @return T
     */
    T selectByIdForUpdate(ID id);

    /**
     * findById 加锁版本
     *
     * @param id id
     * @return Optional<T>
     */
    Optional<T> findByIdForUpdate(ID id);

    /**
     * 根据id查询
     * @param id id
     * @return Optional<T>
     */
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    Optional<T> findById(@NonNull ID id);

    /**
     * 根据id查询
     * @param id id
     * @return T
     */
    @Nullable
    T selectById(ID id);

}
