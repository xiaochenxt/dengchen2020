package io.github.dengchen2020.jpa.base;

import jakarta.persistence.Version;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * 简单CRUD的jpa操作接口
 *
 * @author xiaochen
 * @since 2019/2/28 11:03
 */
@NullMarked
@NoRepositoryBean
public interface CrudJpaRepository<T, ID> extends JpaRepository<T, ID>, QuerydslPredicateExecutor<T> {

    /**
     * 删除-支持批量
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    @Modifying
    @Query("delete from #{#entityName} e where e.id in ?1")
    int delete(Iterable<ID> ids);

    /**
     * 删除-支持批量
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    @Modifying
    @Query("delete from #{#entityName} e where e.id in ?1")
    int delete(ID... ids);

    /**
     * 根据id集合查询
     * @param ids id集合
     * @return List<T>
     */
    @Query("select e from #{#entityName} e where e.id in ?1")
    List<T> selectInIds(Iterable<ID> ids);

    /**
     * 根据id集合查询
     * @param ids id集合
     * @return List<T>
     */
    @Query("select e from #{#entityName} e where e.id in ?1")
    List<T> selectInIds(ID... ids);

    /**
     * 根据id查询记录是否存在
     * @param id id
     * @return true：存在，false：不存在
     */
    @Query("select exists(select 1 from #{#entityName} e where e.id = ?1)")
    boolean exists(ID id);

    /**
     * 用于新增或修改，详见：{@link SimpleJpaRepository#save(Object)}
     * <p> 如果实体有字段使用了 {@link Version} 注解会进行乐观锁校验 </p>
     * <p> 注意： </p>
     * <pre>
     * 1. 使用该方法修改，禁止手动赋值乐观锁字段，如以下操作：
     *     {@code entity.setVersion(0L);}</p>
     * 2. 在事务中使用该方法保存不是实时提交的，是在事务提交时{@link #flush}后才会立即提交到数据库，如果事务中后续还有其他远程调用操作（如接口请求、redis、消息队列等）且需要保证该数据保存成功后才能执行的
     * ，必须使用 {@link #saveAndFlush(Object)} 否则可能导致数据不一致，也可将远程调用操作移到事务外，推荐使用编程式事务，可减小事务粒度，提高性能。
     * </pre>
     *
     * @param entity 不得为 {@code null}。
     * @return 保存的实体，永远不会为 {@code null}。
     * @throws IllegalArgumentException 如果给定的 {@code entity} 为 {@code null}。
     * @throws OptimisticLockingFailureException 当实体使用乐观锁定并且具有 version 属性
     * 与持久化存储中的值不同。如果实体假定存在，但数据库中不存在。
     */
    @NonNull
    @Override
    <S extends T> S save(@NonNull S entity);

    /**
     * 批量保存，详见：{@link BaseJpaRepositoryExecutor#saveAll(Iterable)}
     * <p> 如果实体有字段使用了 {@link Version} 注解会进行乐观锁校验 </p>
     * <p> 注意： </p>
     * <pre>
     * 1. 使用该方法修改，禁止手动赋值乐观锁字段，如以下操作：
     *     {@code entity.setVersion(0L);}</p>
     * 2. 在事务中使用该方法保存不是实时提交的，是在事务提交时{@link #flush}后才会立即提交到数据库，如果事务中后续还有其他远程调用操作（如接口请求、redis、消息队列等）且需要保证该数据保存成功后才能执行的
     * ，必须使用 {@link #saveAllAndFlush(Iterable)} 否则可能导致数据不一致，也可将远程调用操作移到事务外，推荐使用编程式事务，可减小事务粒度，提高性能。
     * </pre>
     *
     * @param entities 不得为 {@code null}，也不得包含 {@code null}。
     * @return 保存的实体，永远不会为 {@code null}。
     * @throws IllegalArgumentException 如果给定的 {@link Iterable entities} 或其实体之一为{@code null} 中。
     * @throws OptimisticLockingFailureException 当至少一个实体使用乐观锁定并且具有版本
     * 属性的值与持久存储中的值不同。如果至少有一个实体假定存在，但数据库中不存在。
     */
    @Override
    <S extends T> List<S> saveAll(Iterable<S> entities);
}
