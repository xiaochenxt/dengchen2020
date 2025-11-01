package io.github.dengchen2020.jpa.base;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * 逻辑删除
 * @author xiaochen
 * @since 2025/3/28
 */
@NullMarked
@NoRepositoryBean
public interface SoftDeleteRepository<T, ID> {

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    int softDelete(Iterable<ID> ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    int softDelete(ID... ids);

    /**
     * 逻辑删除
     *
     * @param id id
     * @return 受影响的行数
     */
    int softDelete(ID id);

}
