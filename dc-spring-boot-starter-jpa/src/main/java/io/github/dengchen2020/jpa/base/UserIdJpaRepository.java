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
    @Query("select e from #{#entityName} e where e.id = ?1 and e.userId = ?#{userId}")
    T selectByIdWithUserId(ID id);

    /**
     * 根据id查询（携带用户id）
     * @param id id
     * @return T
     */
    @Query("select e from #{#entityName} e where e.id = ?1 and e.userId = ?#{userId}")
    Optional<T> findByIdWithUserId(ID id);

    /**
     * 根据id查询（携带用户id）
     * @param ids ids
     * @return T
     */
    @Query("select e from #{#entityName} e where e.id in ?1 and e.userId = ?#{userId}")
    List<T> selectInIdsWithUserId(Iterable<ID> ids);

    /**
     * 根据id查询（携带用户id）
     * @param ids ids
     * @return T
     */
    @Query("select e from #{#entityName} e where e.id in ?1 and e.userId = ?#{userId}")
    List<T> selectInIdsWithUserId(ID... ids);

    /**
     * 根据id删除（携带用户id）
     * @param id id
     * @return 受影响的条数
     */
    @Transactional
    @Modifying
    @Query("delete from #{#entityName} e where e.id = ?1 and e.userId = ?#{userId}")
    int deleteWithUserId(ID id);

    /**
     * 根据id删除（携带用户id）
     * @param ids ids
     * @return 受影响的条数
     */
    @Transactional
    @Modifying
    @Query("delete from #{#entityName} e where e.id in ?1 and e.userId = ?#{userId}")
    int deleteWithUserId(Iterable<ID> ids);

    /**
     * 根据id删除（携带用户id）
     * @param ids ids
     * @return 受影响的条数
     */
    @Transactional
    @Modifying
    @Query("delete from #{#entityName} e where e.id in ?1 and e.userId = ?#{userId}")
    int deleteWithUserId(ID... ids);

}
