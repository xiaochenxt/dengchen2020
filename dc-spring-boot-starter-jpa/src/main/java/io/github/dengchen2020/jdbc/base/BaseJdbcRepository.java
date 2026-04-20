package io.github.dengchen2020.jdbc.base;

import org.jspecify.annotations.NullMarked;

/**
 * JDBC操作通用接口
 * @author xiaochen
 * @since 2025/12/8
 */
@NullMarked
public interface BaseJdbcRepository<T, ID> extends QuerydslJdbcRepository<T>, JdbcClientRepository {



}
