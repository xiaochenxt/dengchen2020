package io.github.dengchen2020.jpa.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
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
public interface QueryDslJpaRepository<T> {

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
    JPAUpdateClause update(Predicate where);

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
    long delete(Predicate where);

}
