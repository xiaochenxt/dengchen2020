package io.github.dengchen2020.jpa.base;

import jakarta.annotation.Nonnull;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * 条件携带租户id的jpa操作接口
 * @author xiaochen
 * @since 2024/12/7
 */
@NoRepositoryBean
public interface TenantJpaRepository<T, ID> {

    /**
     * 根据id查询（携带租户id）
     * @param id id
     * @return T
     */
    T selectByIdWithTenantId(@Nonnull ID id);

    /**
     * 根据id查询（携带租户id）
     * @param ids ids
     * @return T
     */
    List<T> selectInIdsWithTenantId(@Nonnull Iterable<ID> ids);

    /**
     * 根据id查询（携带租户id）
     * @param ids ids
     * @return T
     */
    List<T> selectInIdsWithTenantId(@Nonnull ID... ids);

    /**
     * 根据id查询（携带租户id）
     * @param id id
     * @return T
     */
    Optional<T> findByIdWithTenantId(@Nonnull ID id);

    /**
     * 根据id删除（携带租户id）
     * @param id id
     * @return 受影响的条数
     */
    int deleteWithTenantId(@Nonnull ID id);

    /**
     * 根据id删除（携带租户id）
     * @param ids ids
     * @return 受影响的条数
     */
    int deleteWithTenantId(@Nonnull Iterable<ID> ids);

    /**
     * 根据id删除（携带租户id）
     * @param ids ids
     * @return 受影响的条数
     */
    int deleteWithTenantId(@Nonnull ID... ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    int softDeleteWithTenantId(@Nonnull Iterable<ID> ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    int softDeleteWithTenantId(@Nonnull ID... ids);

    /**
     * 逻辑删除
     *
     * @param id id
     * @return 受影响的行数
     */
    int softDeleteWithTenantId(@Nonnull ID id);

}
