package io.github.dengchen2020.jpa.base;

import jakarta.persistence.EntityManager;
import org.springframework.data.repository.CrudRepository;

/**
 * 为存储库中使用JPA的{@link EntityManager}提供便捷
 * @author xiaochen
 * @since 2025/11/13
 */
public interface EntityManagerRepository<T, ID> {

    /**
     * 清除持久上下文中缓存的JPA实体（可以理解为清除一级缓存），后续的实体的修改不再自动提交到数据库，一般建议手动调用{@link CrudRepository#save(Object)}以将修改提交到数据库 </br>
     * 详见：{@link EntityManager#detach(Object)}
     * @param entity
     */
    void detach(T entity);

}
