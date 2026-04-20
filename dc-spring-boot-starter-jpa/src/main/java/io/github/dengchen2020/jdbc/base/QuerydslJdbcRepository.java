package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
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

    SQLQueryFactory nativeQueryFactory();

    SQLQuery<?> nativeQuery();

    <R> SQLQuery<R> nativeSelect(Expression<R> expr);

    SQLQuery<Tuple> nativeSelect(Expression<?>... exprs);

    <R> SQLQuery<R> nativeSelectDistinct(Expression<R> expr);

    SQLQuery<Tuple> nativeSelectDistinct(Expression<?>... exprs);

    SQLQuery<Integer> nativeSelectOne();

    SQLQuery<Integer> nativeSelectZero();

    /**
     * 单表数据查询
     * @return {@link SQLQuery<T>}
     */
    SQLQuery<T> nativeSelectFrom();

}
