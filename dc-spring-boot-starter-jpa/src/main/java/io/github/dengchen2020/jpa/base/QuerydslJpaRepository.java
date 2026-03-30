package io.github.dengchen2020.jpa.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
import io.github.dengchen2020.jpa.querydsl.NativeQuery;
import io.github.dengchen2020.jpa.querydsl.NativeQueryFactory;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * <p>
 * <pre>所有的操作自带from当前实体对应的表，不允许再from其他表，否则形成笛卡尔积查询</pre>
 * </p>
 * @author xiaochen
 * @since 2024/11/27
 */
@NullMarked
@NoRepositoryBean
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
    default JPAUpdateClause update(Predicate where){
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
    default long delete(Predicate where){
        return delete(new Predicate[]{where});
    }

    /**
     * 原生SQL查询，所有的操作自带from当前实体对应的表，不允许再from其他表，否则形成笛卡尔积查询
     * @return {@link NativeQuery <T>}
     */
    NativeQuery<T> nativeQuery();

    /**
     * 原生SQL查询工厂，与{@link #nativeQuery}不同的是，不自带from当前实体对应的表。原生SQL支持子查询等复杂SQL，更加灵活自由
     * @return {@link NativeQuery<T>}
     */
    NativeQueryFactory nativeQueryFactory();

}
