package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLUpdateClause;
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

    <R> SQLQuery<R> select(Expression<R> expr);

    SQLQuery<Tuple> select(Expression<?>... exprs);

    <R> SQLQuery<R> selectDistinct(Expression<R> expr);

    SQLQuery<Tuple> selectDistinct(Expression<?>... exprs);

    SQLQuery<Integer> selectOne();

    SQLQuery<Integer> selectZero();

    /**
     * 单表数据查询
     * @return {@link SQLQuery<T>}
     */
    SQLQuery<T> selectFrom();

    /**
     * 更新构造
     *
     * @param where 更新条件
     * @return {@link SQLUpdateClause}
     */
    SQLUpdateClause update(Predicate[] where);

    /**
     * 更新构造
     *
     * @param where 更新条件
     * @return {@link SQLUpdateClause}
     */
    default SQLUpdateClause update(Predicate where) {
        return update(new Predicate[]{where});
    }

    /**
     * 删除构造
     *
     * @param where 删除条件
     * @return 受影响的行数
     */
    long delete(Predicate[] where);

    /**
     * 删除构造
     *
     * @param where 删除条件
     * @return 受影响的行数
     */
    default long delete(Predicate where) {
        return delete(new Predicate[]{where});
    }

}
