package io.github.dengchen2020.jdbc.base;

import org.jspecify.annotations.NullMarked;

/**
 * 逻辑删除
 * @author xiaochen
 * @since 2025/3/28
 */
@NullMarked
interface SoftDeleteJdbcRepository<T, ID> {

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    long softDelete(Iterable<ID> ids);

    /**
     * 批量逻辑删除
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    long softDelete(ID... ids);

    /**
     * 逻辑删除
     *
     * @param id id
     * @return 受影响的行数
     */
    long softDelete(ID id);

}
