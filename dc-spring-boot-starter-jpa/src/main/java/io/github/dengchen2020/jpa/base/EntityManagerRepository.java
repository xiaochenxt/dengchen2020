package io.github.dengchen2020.jpa.base;

import jakarta.persistence.EntityManager;
import org.springframework.data.repository.CrudRepository;

import java.util.function.Function;

/**
 * 为存储库中使用JPA的{@link EntityManager}提供便捷
 * @author xiaochen
 * @since 2025/11/13
 */
public interface EntityManagerRepository {

    /**
     * 操作{@link EntityManager}并返回执行结果
     */
    <R> R execute(Function<EntityManager, R> function);

    /**
     * 清除持久上下文中缓存的JPA实体（可以理解为清除一级缓存），后续的实体的修改不再自动提交到数据库，一般建议手动调用{@link CrudRepository#save(Object)}以将修改提交到数据库 </br>
     * 详见：{@link EntityManager#detach(Object)}
     * @param entity
     */
    void detach(Object entity);

}
