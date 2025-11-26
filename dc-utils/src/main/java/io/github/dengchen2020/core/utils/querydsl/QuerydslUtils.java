package io.github.dengchen2020.core.utils.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import org.jspecify.annotations.NullMarked;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Querydsl工具类
 * @author xiaochen
 * @since 2025/11/24
 */
@NullMarked
public class QuerydslUtils {

    public static final Expression<?>[] EMPTY_EXPRESSIONS = new Expression[0];

    private static final ConcurrentMap<String, Expression<?>[]> EXPRESSION_CACHE = new ConcurrentReferenceHashMap<>();

    private static final ConcurrentMap<Class<?>, Map<String, Expression<?>>> Q_ENTITY_FIELD_CACHE = new ConcurrentReferenceHashMap<>(32, ConcurrentReferenceHashMap.ReferenceType.WEAK);

    /**
     * 为给定类型和Q类创建setter调用投影所需的表达式
     * @param dtoType 投影类型
     * @param entity Q类实例
     * @return 表达式数组
     */
    public static <T> Expression<?>[] generateSetterExpressions(
            Class<? extends T> dtoType, EntityPath<?> entity) {
        var entityClass = entity.getClass();
        var cacheKey = dtoType.getName() + ":setter:" + entityClass.getName();
        var cache = EXPRESSION_CACHE.get(cacheKey);
        if (cache != null) return cache;

        var entityFieldMap = getEntityFieldMap(entityClass, entity);
        List<Expression<?>> expressions = new ArrayList<>();
        var propertyDescriptor = ReflectUtils.getBeanSetters(dtoType);
        for (PropertyDescriptor descriptor : propertyDescriptor) {
            Expression<?> expression = entityFieldMap.get(descriptor.getName());
            if (expression != null) expressions.add(expression);
        }

        var expressionsArray = expressions.toArray(EMPTY_EXPRESSIONS);
        EXPRESSION_CACHE.put(cacheKey, expressionsArray);
        return expressionsArray;
    }

    /**
     * 为给定Record类型和Q类创建构造调用投影所需的表达式
     *
     * @param dtoType 投影类型
     * @param entity  Q类实例
     * @return 表达式数组
     */
    public static <T extends Record> Expression<?>[] generateRecordConstructorExpressions(
            Class<T> dtoType, EntityPath<?> entity) {
        var entityClass = entity.getClass();
        var cacheKey = dtoType.getName() + "record:" + entityClass.getName();
        var cache = EXPRESSION_CACHE.get(cacheKey);
        if (cache != null) return cache;

        var entityFieldMap = getEntityFieldMap(entityClass, entity);
        RecordComponent[] recordComponents = dtoType.getRecordComponents();
        List<Expression<?>> expressions = new ArrayList<>(recordComponents.length);

        for (RecordComponent recordComponent : recordComponents) {
            Expression<?> expression = entityFieldMap.get(recordComponent.getName());
            if (expression != null) expressions.add(expression);
        }

        var expressionsArray = expressions.toArray(EMPTY_EXPRESSIONS);
        EXPRESSION_CACHE.put(cacheKey, expressionsArray);
        return expressionsArray;
    }

    private static Map<String, Expression<?>> getEntityFieldMap(Class<?> entityClass, EntityPath<?> entity) {
        return Q_ENTITY_FIELD_CACHE.computeIfAbsent(entityClass, clazz -> {
            Map<String, Expression<?>> fieldMap = new HashMap<>();
            ReflectionUtils.doWithLocalFields(clazz, field -> {
                if (Expression.class.isAssignableFrom(field.getType())) {
                    try {
                        fieldMap.put(field.getName(), (Expression<?>) field.get(entity));
                    } catch (IllegalAccessException ignored) {}
                }
            });
            return fieldMap;
        });
    }

    public static void clearCache() {
        EXPRESSION_CACHE.clear();
        Q_ENTITY_FIELD_CACHE.clear();
    }

}
