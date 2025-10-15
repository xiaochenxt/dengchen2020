package io.github.dengchen2020.jpa.base;

import jakarta.annotation.Nonnull;
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
@NoRepositoryBean
public interface QueryJpaRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * selectById 加锁版本
     *
     * @param id id
     * @return T
     */
    T selectByIdForUpdate(@Nonnull ID id);

    /**
     * findById 加锁版本
     *
     * @param id id
     * @return Optional<T>
     */
    @Nonnull
    Optional<T> findByIdForUpdate(@Nonnull ID id);

    /**
     * 根据id查询
     * @param id id
     * @return Optional<T>
     */
    @Nonnull
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    Optional<T> findById(@Nonnull ID id);

    /**
     * 根据id查询
     * @param id id
     * @return T
     */
    T selectById(@Nonnull ID id);

}
