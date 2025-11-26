package io.github.dengchen2020.core.utils.querydsl;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.QBean;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

import static io.github.dengchen2020.core.utils.querydsl.QuerydslUtils.*;

/**
 * 提取Querydsl中常用的投影方式并扩展为更易使用的投影方式，可在绝大多数场景下替代源{@link com.querydsl.core.types.Projections}
 * @author xiaochen
 * @since 2025/11/24
 */
@NullMarked
public class Projections {

    /**
     * 为给定类型和表达式创建一个setter赋值的投影
     * <p>注意：{@code type}类定义必须符合JavaBeans规范</p>
     * <p>示例
     *
     * <pre>
     * UserDTO dto = query.select(
     *     Projections.bean(UserDTO.class, user.firstName, user.lastName));
     * </pre>
     *
     * @param <T> 投影类型
     * @param type 投影类型
     * @param exprs 投影的表达式
     * @return 工厂表达式
     */
    public static <T> QBean<T> bean(Class<? extends T> type, Expression<?>... exprs) {
        return com.querydsl.core.types.Projections.bean(type, exprs);
    }

    /**
     * 为给定类型和表达式创建一个setter赋值的投影，主要用于连表查询
     * <p>注意：{@code type}类定义必须符合JavaBeans规范</p>
     * <p>示例
     *
     * <pre>
     * UserDTO dto = query.select(
     *     Projections.bean(OrderDTO.class, order, user.firstName, user.lastName));
     * </pre>
     *
     * @param <T> 投影类型
     * @param type 投影类型
     * @param entityPath 连表查询时的主表Q类
     * @param exprs 投影的表达式
     * @return 工厂表达式
     */
    public static <T> QBean<T> bean(Class<? extends T> type, EntityPath<T> entityPath, Expression<?>... exprs) {
        List<Expression<?>> expressions = new ArrayList<>();
        Expression<?>[] generateSetterExpressions = generateSetterExpressions(type, entityPath);
        for (int i = 0, generateSetterExpressionsLength = generateSetterExpressions.length; i < generateSetterExpressionsLength; i++) {
            expressions.add(generateSetterExpressions[i]);
        }
        for (int i = 0, exprsLength = exprs.length; i < exprsLength; i++) {
            Expression<?> expr = exprs[i];
            expressions.add(expr);
        }
        if (!expressions.isEmpty()) return com.querydsl.core.types.Projections.bean(type, expressions.toArray(EMPTY_EXPRESSIONS));
        return com.querydsl.core.types.Projections.bean(type, exprs);
    }

    /**
     * 为给定类型和Q类创建一个setter调用投影
     * <p>注意：{@code type}类定义必须符合JavaBeans规范，setter方法参数类型需与Q类中对应字段的泛型类型一致</p>
     * <p>示例
     *
     * <pre>
     * UserDTO dto = query.select(
     *     Projections.bean(UserDTO.class, user));
     * </pre>
     *
     * @param <T> 投影类型
     * @param type 投影类型
     * @param entityPath Q类
     * @return 工厂表达式
     */
    public static <T> QBean<T> bean(Class<? extends T> type, EntityPath<?> entityPath) {
        return com.querydsl.core.types.Projections.bean(type, generateSetterExpressions(type, entityPath));
    }

    /**
     * 为给定类型和表达式创建构造调用投影
     *
     * <p>示例
     *
     * <pre>
     * UserDTO dto = query.singleResult(
     *     Projections.constructor(UserDTO.class, user.firstName, user.lastName));
     * </pre>
     *
     * @param <T> 投影类型
     * @param type 投影类型
     * @param exprs 投影的表达式
     * @return factory expression
     */
    public static <T> ConstructorExpression<T> constructor(
            Class<? extends T> type, Expression<?>... exprs) {
        return com.querydsl.core.types.Projections.constructor(type, exprs);
    }

    /**
     * 为给定Record类型和Q类创建构造调用投影
     * <p>注意：{@code type}类中的字段名和类型需与Q类中对应字段的泛型类型一致</p>
     * <p>示例
     *
     * <pre>
     * UserDTO dto = query.singleResult(
     *     Projections.constructor(UserDTO.class, user));
     * </pre>
     *
     * @param <T> 投影类型
     * @param type 投影类型
     * @param entityPath Q类
     * @return factory expression
     */
    public static <T extends Record> ConstructorExpression<T> constructor(
            Class<T> type, EntityPath<?> entityPath) {
        return com.querydsl.core.types.Projections.constructor(type, generateRecordConstructorExpressions(type, entityPath));
    }

}
