package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;

import java.util.List;
import java.util.Optional;

/**
 * querydsl查询操作
 * @author xiaochen
 * @since 2025/12/9
 */
interface QuerydslPredicateExecutor<T> {

    Optional<T> findOne(Predicate predicate);

    List<T> findAll(Predicate predicate);

    List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);

    List<T> findAll(OrderSpecifier<?>... orders);

    long count(Predicate predicate);

    boolean exists(Predicate predicate);

}
