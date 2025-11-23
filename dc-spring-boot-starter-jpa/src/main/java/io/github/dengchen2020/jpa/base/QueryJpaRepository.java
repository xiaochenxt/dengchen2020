package io.github.dengchen2020.jpa.base;

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
     * 根据id查询并给这条数据加悲观排他锁直到事务完成时自动释放，因此必须在事务中执行，否则视为普通快照查询
     * <p>简述：当前事务未完成时，其他事务对这条数据的for update查询、for share查询（普通快照查询不受影响）、修改、删除操作会被阻塞等待</p>
     *
     * @param id id
     * @return T
     */
    @Nullable
    T selectByIdForUpdate(ID id);

    /**
     * 根据id查询并给这条数据加悲观排他锁直到事务完成时自动释放，因此必须在事务中执行，否则视为普通快照查询
     * <p>简述：当前事务未完成时，其他事务对这条数据的for update查询、for share查询（普通快照查询不受影响）、修改、删除操作会被阻塞等待</p>
     *
     * @param id id
     * @return {@link Optional<T>}
     */
    Optional<T> findByIdForUpdate(ID id);

    /**
     * 与{@link #selectByIdForUpdate(Object)}不同的是，该方法不阻塞等待获取锁，而是获取锁失败时抛出异常
     *
     * @param id id
     * @return T
     */
    @Nullable
    T selectByIdForUpdateNowait(ID id);

    /**
     * 与{@link #findByIdForUpdate(Object)}不同的是，该方法不阻塞等待获取锁，而是获取锁失败时抛出异常
     *
     * @param id id
     * @return {@link Optional<T>}
     */
    Optional<T> findByIdForUpdateNowait(ID id);

    /**
     * 根据id查询
     * @param id id
     * @return {@link Optional<T>}
     */
    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    Optional<T> findById(ID id);

    /**
     * 根据id查询
     * @param id id
     * @return T
     */
    @Nullable
    T selectById(ID id);

    /**
     * 根据id查询并给这条数据加悲观共享锁直到事务完成时自动释放，因此必须在事务中执行，否则视为普通快照查询
     * <p>简述：当前事务未完成时，其他事务对这条数据的for update查询（for share查询和普通快照查询不受影响）、修改、删除操作会被阻塞等待</p>
     * @param id id
     * @return T
     */
    @Nullable
    T selectByIdForShare(ID id);

    /**
     * 根据id查询并给这条数据加悲观共享锁直到事务完成时自动释放，因此必须在事务中执行，否则视为普通快照查询
     * <p>简述：当前事务未完成时，其他事务对这条数据的for update查询（for share查询和普通快照查询不受影响）、修改、删除操作会被阻塞等待</p>
     * @param id id
     * @return {@link Optional<T>}
     */
    Optional<T> findByIdForShare(ID id);

    /**
     * 与{@link #selectByIdForShare(Object)}不同的是，该方法不阻塞等待获取锁，而是获取锁失败时抛出异常
     * @param id id
     * @return T
     */
    @Nullable
    T selectByIdForShareNowait(ID id);

    /**
     * 与{@link #findByIdForShare(Object)}不同的是，该方法不阻塞等待获取锁，而是获取锁失败时抛出异常
     * @param id id
     * @return {@link Optional<T>}
     */
    Optional<T> findByIdForShareNowait(ID id);

}
