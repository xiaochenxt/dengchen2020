package io.github.dengchen2020.jdbc.base;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 条件携带租户id的jdbc操作接口
 * @author xiaochen
 * @since 2024/12/7
 */
@NullMarked
interface TenantJdbcRepository<T, ID> {

    /**
     * 根据id查询（携带租户id）
     * @param id id
     * @return T
     */
    @Nullable
    T selectByIdWithTenantId(ID id);

    /**
     * 根据id查询（携带租户id）
     * @param ids ids
     * @return T
     */
    List<T> selectInIdsWithTenantId(Iterable<ID> ids);

    /**
     * 根据id查询（携带租户id）
     * @param ids ids
     * @return T
     */
    List<T> selectInIdsWithTenantId(ID... ids);

    /**
     * 根据id查询（携带租户id）
     * @param id id
     * @return T
     */
    Optional<T> findByIdWithTenantId(ID id);

    /**
     * 根据id删除（携带租户id）
     * @param id id
     * @return 受影响的条数
     */
    long deleteWithTenantId(ID id);

    /**
     * 根据id删除（携带租户id）
     * @param ids ids
     * @return 受影响的条数
     */
    long deleteWithTenantId(Iterable<ID> ids);

    /**
     * 根据id删除（携带租户id）
     * @param ids ids
     * @return 受影响的条数
     */
    long deleteWithTenantId(ID... ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    long softDeleteWithTenantId(Iterable<ID> ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    long softDeleteWithTenantId(ID... ids);

    /**
     * 逻辑删除
     *
     * @param id id
     * @return 受影响的行数
     */
    long softDeleteWithTenantId(ID id);

}
