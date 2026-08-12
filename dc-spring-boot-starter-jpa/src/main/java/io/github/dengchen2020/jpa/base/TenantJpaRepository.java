package io.github.dengchen2020.jpa.base;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 条件携带租户id的jpa操作接口
 * @author xiaochen
 * @since 2024/12/7
 */
@NullMarked
@NoRepositoryBean
public interface TenantJpaRepository<T, ID> {

    /**
     * 根据id查询（携带租户id）
     * @param id id
     * @return T
     */
    @Nullable
    @Query("select e from #{#entityName} e where id(e) = :id and e.tenantId = ?#{tenantId}")
    T selectByIdWithTenantId(ID id);

    /**
     * 根据id查询（携带租户id）
     * @param id id
     * @return T
     */
    @Query("select e from #{#entityName} e where id(e) = :id and e.tenantId = ?#{tenantId}")
    default Optional<T> findByIdWithTenantId(ID id) {
        return Optional.ofNullable(selectByIdWithTenantId(id));
    }

    /**
     * 根据id查询（携带租户id）
     * @param ids ids
     * @return T
     */
    @Query("select e from #{#entityName} e where id(e) in :ids and e.tenantId = ?#{tenantId}")
    List<T> selectInIdsWithTenantId(Iterable<ID> ids);

    /**
     * 根据id查询（携带租户id）
     * @param ids ids
     * @return T
     */
    @Query("select e from #{#entityName} e where id(e) in :ids and e.tenantId = ?#{tenantId}")
    List<T> selectInIdsWithTenantId(ID... ids);

    /**
     * 根据id删除（携带租户id）
     * @param id id
     * @return 受影响的条数
     */
    @Transactional
    @Modifying
    @Query("delete from #{#entityName} e where id(e) = :id and e.tenantId = ?#{tenantId}")
    int deleteWithTenantId(ID id);

    /**
     * 根据id删除（携带租户id）
     * @param ids ids
     * @return 受影响的条数
     */
    @Transactional
    @Modifying
    @Query("delete from #{#entityName} e where id(e) in :ids and e.tenantId = ?#{tenantId}")
    int deleteWithTenantId(Iterable<ID> ids);

    /**
     * 根据id删除（携带租户id）
     * @param ids ids
     * @return 受影响的条数
     */
    @Transactional
    @Modifying
    @Query("delete from #{#entityName} e where id(e) in :ids and e.tenantId = ?#{tenantId}")
    int deleteWithTenantId(ID... ids);

}
