package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.WithBuilder;
import jakarta.persistence.EntityManager;

/**
 * 原生SQL查询工厂
 * @author xiaochen
 * @since 2026/3/29
 */
final class NativeQueryFactory {

    private final EntityManager entityManager;
    private final SQLTemplates templates;

    public NativeQueryFactory(EntityManager entityManager, SQLTemplates templates) {
        this.entityManager = entityManager;
        this.templates = templates;
    }

    public JPASQLQuery<?> query() {
        return new DcJPASQLQuery<>(entityManager, templates);
    }

    public <T> JPASQLQuery<T> select(Expression<T> expr) {
        return query().select(expr);
    }

    public JPASQLQuery<Tuple> select(Expression<?>... exprs) {
        return query().select(exprs);
    }

    public <T> JPASQLQuery<T> selectDistinct(Expression<T> expr) {
        return query().select(expr).distinct();
    }

    public JPASQLQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return query().select(exprs).distinct();
    }

    public JPASQLQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    public JPASQLQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    public JPASQLQuery<?> with(EntityPath<?> path, SubQueryExpression<?> query) {
        return query().with(path, query);
    }

    public final WithBuilder<? extends JPASQLQuery<?>> with(Path<?> alias, Path<?>... columns) {
        return query().with(alias, columns);
    }

    public JPASQLQuery<?> with(Path<?> alias, Expression<?> query) {
        return query().with(alias, query);
    }

    public JPASQLQuery<?> withRecursive(EntityPath<?> path, SubQueryExpression<?> query) {
        return query().withRecursive(path, query);
    }

    public final WithBuilder<? extends JPASQLQuery<?>> withRecursive(EntityPath<?> alias, Path<?>... columns) {
        return query().withRecursive(alias, columns);
    }

    public JPASQLQuery<?> withRecursive(Path<?> alias, Expression<?> query) {
        return query().withRecursive(alias, query);
    }

}