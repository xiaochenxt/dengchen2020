package io.github.dengchen2020.jpa.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.hibernate.sql.HibernateSQLQuery;
import org.jspecify.annotations.NullMarked;

/**
 * <p>
 * <pre>所有的操作自带from当前实体对应的表，不允许再from其他表，否则形成笛卡尔积查询</pre>
 * </p>
 * @author xiaochen
 * @since 2024/11/27
 */
@NullMarked
public final class NativeQuery<T> {

    private final NativeQueryFactory queryFactory;
    private final EntityPath<T> path;

    public NativeQuery(NativeQueryFactory queryFactory, EntityPath<T> path) {
        this.queryFactory = queryFactory;
        this.path = path;
    }

    public <R> HibernateSQLQuery<R> select(Expression<R> expr) {
        return queryFactory.select(expr).from(path);
    }

    public HibernateSQLQuery<Tuple> select(Expression<?>... exprs) {
        return queryFactory.select(exprs).from(path);
    }

    public <R> HibernateSQLQuery<R> selectDistinct(Expression<R> expr) {
        return queryFactory.select(expr).distinct().from(path);
    }

    public HibernateSQLQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return queryFactory.select(exprs).distinct().from(path);
    }

    public HibernateSQLQuery<Integer> selectOne() {
        return queryFactory.select(Expressions.ONE).from(path);
    }

    public HibernateSQLQuery<Integer> selectZero() {
        return queryFactory.select(Expressions.ZERO).from(path);
    }

    public HibernateSQLQuery<T> selectFrom() {
        return queryFactory.select(path).from(path);
    }

}
