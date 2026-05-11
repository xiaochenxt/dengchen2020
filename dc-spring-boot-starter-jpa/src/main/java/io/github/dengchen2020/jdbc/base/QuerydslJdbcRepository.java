package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.sql.JPASQLQuery;
import org.jspecify.annotations.NullMarked;

/**
 * <p>
 * <pre>所有的操作自带from当前实体对应的表，不允许再from其他表，否则形成笛卡尔积查询</pre>
 * </p>
 * @author xiaochen
 * @since 2024/11/27
 */
@NullMarked
interface QuerydslJdbcRepository<T> {

    <R> JPASQLQuery<R> nativeSelect(Expression<R> expr);

    JPASQLQuery<Tuple> nativeSelect(Expression<?>... exprs);

}
