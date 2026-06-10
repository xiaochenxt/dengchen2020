package io.github.dengchen2020.jpa.base;

import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 为存储库中使用JPA的{@link EntityManager}提供便捷
 * @author xiaochen
 * @since 2025/11/13
 */
@NullMarked
public interface EntityManagerRepository<T> {

    /**
     * 清除持久化上下文，导致所有被托管的实体将变得分离。对实体所做的更改
     * 未被冲入数据库将不会被记录持续存在。推荐在批量处理数据时避免内存溢出需要及时释放内存时使用
     */
    void clear();

    /**
     * 清除持久上下文中缓存的JPA实体，后续的实体的修改不再自动提交到数据库，可以手动调用{@link JpaRepository#saveAndFlush(Object)}以将修改提交到数据库 </br>
     * 详见：{@link EntityManager#detach(Object)}
     * @param entity
     */
    void detach(T entity);

}
