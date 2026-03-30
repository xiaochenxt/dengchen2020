package io.github.dengchen2020.jpa.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.hibernate.sql.HibernateSQLQuery;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.WithBuilder;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

/**
 * 原生SQL查询工厂
 * @author xiaochen
 * @since 2026/3/29
 */
public class NativeQueryFactory {

    private final Session session;
    private final SQLTemplates templates;

    public NativeQueryFactory(EntityManager entityManager, SQLTemplates templates) {
        this.session = entityManager.unwrap(Session.class);
        this.templates = templates;
    }

    public HibernateSQLQuery<?> query() {
        return new HibernateSQLQuery<>(session, templates);
    }

    public <T> HibernateSQLQuery<T> select(Expression<T> expr) {
        return query().select(expr);
    }

    public HibernateSQLQuery<Tuple> select(Expression<?>... exprs) {
        return query().select(exprs);
    }

    public <T> HibernateSQLQuery<T> selectDistinct(Expression<T> expr) {
        return query().select(expr).distinct();
    }

    public HibernateSQLQuery<Tuple> selectDistinct(Expression<?>... exprs) {
        return query().select(exprs).distinct();
    }

    public HibernateSQLQuery<Integer> selectZero() {
        return select(Expressions.ZERO);
    }

    public HibernateSQLQuery<Integer> selectOne() {
        return select(Expressions.ONE);
    }

    public <T> HibernateSQLQuery<T> selectFrom(EntityPath<T> expr) {
        return select(expr).from(expr);
    }

    public HibernateSQLQuery<?> with(EntityPath<?> path, SubQueryExpression<?> query) {
        return query().with(path, query);
    }

    public final WithBuilder<? extends HibernateSQLQuery<?>> with(Path<?> alias, Path<?>... columns) {
        return query().with(alias, columns);
    }

    public HibernateSQLQuery<?> with(Path<?> alias, Expression<?> query) {
        return query().with(alias, query);
    }


    public HibernateSQLQuery<?> withRecursive(EntityPath<?> path, SubQueryExpression<?> query) {
        return query().withRecursive(path, query);
    }

    public final WithBuilder<? extends HibernateSQLQuery<?>> withRecursive(EntityPath<?> alias, Path<?>... columns) {
        return query().withRecursive(alias, columns);
    }

    public HibernateSQLQuery<?> withRecursive(Path<?> alias, Expression<?> query) {
        return query().withRecursive(alias, query);
    }

}