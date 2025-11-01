package io.github.dengchen2020.jpa.base;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * 条件携带用户id的jpa操作接口
 * @author xiaochen
 * @since 2024/12/7
 */
@NullMarked
@NoRepositoryBean
public interface UserIdJpaRepository<T, ID> {

    /**
     * 根据id查询（携带用户id）
     * @param id id
     * @return T
     */
    @Nullable
    T selectByIdWithUserId(ID id);

    /**
     * 根据id查询（携带用户id）
     * @param ids ids
     * @return T
     */
    List<T> selectInIdsWithUserId(Iterable<ID> ids);

    /**
     * 根据id查询（携带用户id）
     * @param ids ids
     * @return T
     */
    List<T> selectInIdsWithUserId(ID... ids);

    /**
     * 根据id查询（携带用户id）
     * @param id id
     * @return T
     */
    Optional<T> findByIdWithUserId(ID id);

    /**
     * 根据id删除（携带用户id）
     * @param id id
     * @return 受影响的条数
     */
    int deleteWithUserId(ID id);

    /**
     * 根据id删除（携带用户id）
     * @param ids ids
     * @return 受影响的条数
     */
    int deleteWithUserId(Iterable<ID> ids);

    /**
     * 根据id删除（携带用户id）
     * @param ids ids
     * @return 受影响的条数
     */
    int deleteWithUserId(ID... ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    int softDeleteWithUserId(Iterable<ID> ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    int softDeleteWithUserId(ID... ids);

    /**
     * 逻辑删除
     *
     * @param id id
     * @return 受影响的行数
     */
    int softDeleteWithUserId(ID id);

}
