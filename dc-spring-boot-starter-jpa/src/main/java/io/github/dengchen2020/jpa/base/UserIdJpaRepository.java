package io.github.dengchen2020.jpa.base;

import jakarta.annotation.Nonnull;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * 条件携带用户id的jpa操作接口
 * @author xiaochen
 * @since 2024/12/7
 */
@NoRepositoryBean
public interface UserIdJpaRepository<T, ID> {

    /**
     * 根据id查询（携带用户id）
     * @param id id
     * @return T
     */
    T selectByIdWithUserId(@Nonnull ID id);

    /**
     * 根据id查询（携带用户id）
     * @param ids ids
     * @return T
     */
    List<T> selectInIdsWithUserId(@Nonnull Iterable<ID> ids);

    /**
     * 根据id查询（携带用户id）
     * @param ids ids
     * @return T
     */
    List<T> selectInIdsWithUserId(@Nonnull ID... ids);

    /**
     * 根据id查询（携带用户id）
     * @param id id
     * @return T
     */
    Optional<T> findByIdWithUserId(@Nonnull ID id);

    /**
     * 根据id删除（携带用户id）
     * @param id id
     * @return 受影响的条数
     */
    int deleteWithUserId(@Nonnull ID id);

    /**
     * 根据id删除（携带用户id）
     * @param ids ids
     * @return 受影响的条数
     */
    int deleteWithUserId(@Nonnull Iterable<ID> ids);

    /**
     * 根据id删除（携带用户id）
     * @param ids ids
     * @return 受影响的条数
     */
    int deleteWithUserId(@Nonnull ID... ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    int softDeleteWithUserId(@Nonnull Iterable<ID> ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    int softDeleteWithUserId(@Nonnull ID... ids);

    /**
     * 逻辑删除
     *
     * @param id id
     * @return 受影响的行数
     */
    int softDeleteWithUserId(@Nonnull ID id);

}
