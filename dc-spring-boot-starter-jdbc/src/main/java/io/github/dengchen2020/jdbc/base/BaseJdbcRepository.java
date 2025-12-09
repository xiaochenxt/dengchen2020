package io.github.dengchen2020.jdbc.base;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;

import java.util.List;

/**
 * JDBC操作通用接口
 * @author xiaochen
 * @since 2025/12/8
 */
@NullMarked
public interface BaseJdbcRepository<T, ID> extends QuerydslJdbcRepository<T>, ComplexJdbcRepository<T>, QueryJdbcRepository<T, ID>,
        SoftDeleteJdbcRepository<T, ID>, UserIdJdbcRepository<T, ID>, TenantJdbcRepository<T, ID>,
        ListCrudRepository<T, ID>, ListPagingAndSortingRepository<T, ID>,
        QuerydslPredicateExecutor<T> {

    /**
     * 删除-支持批量
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    long delete(Iterable<ID> ids);

    /**
     * 删除-支持批量
     *
     * @param ids id集合
     * @return 受影响的行数
     */
    long delete(ID... ids);

    /**
     * 根据id集合查询
     * @param ids id集合
     * @return List<T>
     */
    List<T> selectInIds(Iterable<ID> ids);

    /**
     * 根据id集合查询
     * @param ids id集合
     * @return List<T>
     */
    List<T> selectInIds(ID... ids);

    /**
     * 根据id查询记录是否存在
     * @param id id
     * @return true：存在，false：不存在
     */
    boolean exists(ID id);

}
