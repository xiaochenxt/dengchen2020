package io.github.dengchen2020.jpa.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
import org.jspecify.annotations.NullMarked;

/**
 * <p>
 * <pre>所有的操作自带from当前实体对应的表，不允许再from其他表，否则形成笛卡尔积查询</pre>
 * </p>
 * @author xiaochen
 * @since 2024/11/27
 */
@NullMarked
public interface QuerydslJpaRepository<T> {

    <R> JPAQuery<R> select(Expression<R> expr);

    JPAQuery<Tuple> select(Expression<?>... exprs);

    <R> JPAQuery<R> selectDistinct(Expression<R> expr);

    JPAQuery<Tuple> selectDistinct(Expression<?>... exprs);

    JPAQuery<Integer> selectOne();

    JPAQuery<Integer> selectZero();

    /**
     * 单表数据查询
     * @return {@link JPAQuery<T>}
     */
    JPAQuery<T> selectFrom();

    /**
     * 查询{@code type}中与域类同名且拥有setter方法的字段和额外字段
     * <p>{@code type}中与当前域类同名的字段和额外字段会参与查询</p>
     * @param type 结果类型，必须符合JavaBeans规范
     * @param exprs 额外字段，在查询关联表的字段或表达式字段时使用
     * @return {@link JPAQuery<R>}
     */
    <R> JPAQuery<R> selectBean(Class<? extends R> type, Expression<?>... exprs);

    /**
     * 查询{@code type}中与域类同名的字段和额外字段
     * <p>{@code type}中与当前域类同名的字段和额外字段会参与查询</p>
     * @param type 结果类型，必须为public的{@link Record}类，当需要查询额外字段时，域类同名字段在前（无顺序要求），额外字段必须在末尾
     * @param exprs 额外字段（与{@code type}中的对应字段保持顺序一致，类似于构造函数），在查询关联表的字段或表达式字段时使用
     * @return {@link JPAQuery<R>}
     */
    <R extends Record> JPAQuery<R> selectRecord(Class<? extends R> type, Expression<?>... exprs);

    /**
     * 查询{@code type}中构造函数里的字段
     * <p>{@code type}中的构造函数字段会参与查询</p>
     * @param type 结果类型，必须有查询字段对应的public构造函数
     * @param exprs 查询字段，必须与{@code type}中的构造函数参数顺序一致
     * @return {@link JPAQuery<R>}
     */
    default <R> JPAQuery<R> selectConstructor(Class<? extends R> type, Expression<?>... exprs) {
        return select(Projections.constructor(type, exprs));
    }

    /**
     * 更新构造
     *
     * @param where 更新条件
     * @return {@link JPAUpdateClause}
     */
    JPAUpdateClause update(Predicate[] where);

    /**
     * 更新构造
     *
     * @param where 更新条件
     * @return {@link JPAUpdateClause}
     */
    default JPAUpdateClause update(Predicate where) {
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
