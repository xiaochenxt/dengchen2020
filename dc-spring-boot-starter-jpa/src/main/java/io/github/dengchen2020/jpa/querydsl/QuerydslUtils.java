package io.github.dengchen2020.jpa.querydsl;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.util.PrimitiveUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
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
public abstract class QuerydslUtils {

    private QuerydslUtils() {}

    public static final Expression<?>[] EMPTY_EXPRESSIONS = new Expression[0];
    public static final NullExpression<String> NULL_STRING = Expressions.nullExpression(String.class);
    public static final NullExpression<Long> NULL_LONG = Expressions.nullExpression(Long.class);
    public static final NullExpression<Integer> NULL_INTEGER = Expressions.nullExpression(Integer.class);
    public static final NullExpression<Boolean> NULL_BOOLEAN = Expressions.nullExpression(Boolean.class);
    public static final NullExpression<Double> NULL_DOUBLE = Expressions.nullExpression(Double.class);
    public static final NullExpression<Float> NULL_FLOAT = Expressions.nullExpression(Float.class);
    public static final NullExpression<Short> NULL_SHORT = Expressions.nullExpression(Short.class);
    public static final NullExpression<Byte> NULL_BYTE = Expressions.nullExpression(Byte.class);
    public static final NullExpression<Character> NULL_CHARACTER = Expressions.nullExpression(Character.class);
    public static final NullExpression<BigDecimal> NULL_BIG_DECIMAL = Expressions.nullExpression(BigDecimal.class);
    public static final NullExpression<BigInteger> NULL_BIG_INTEGER = Expressions.nullExpression(BigInteger.class);

    private static final Map<Class<?>, NullExpression<?>> primitivesNullExpressions = new HashMap<>();

    static {
        primitivesNullExpressions.put(Long.class, NULL_LONG);
        primitivesNullExpressions.put(Long.TYPE, NULL_LONG);
        primitivesNullExpressions.put(Integer.class, NULL_INTEGER);
        primitivesNullExpressions.put(Integer.TYPE, NULL_INTEGER);
        primitivesNullExpressions.put(Boolean.class, NULL_BOOLEAN);
        primitivesNullExpressions.put(Boolean.TYPE, NULL_BOOLEAN);
        primitivesNullExpressions.put(Double.class, NULL_DOUBLE);
        primitivesNullExpressions.put(Double.TYPE, NULL_DOUBLE);
        primitivesNullExpressions.put(Float.class, NULL_FLOAT);
        primitivesNullExpressions.put(Float.TYPE, NULL_FLOAT);
        primitivesNullExpressions.put(Byte.class, NULL_BYTE);
        primitivesNullExpressions.put(Byte.TYPE, NULL_BYTE);
        primitivesNullExpressions.put(Short.class, NULL_SHORT);
        primitivesNullExpressions.put(Short.TYPE, NULL_SHORT);
        primitivesNullExpressions.put(Character.class, NULL_CHARACTER);
        primitivesNullExpressions.put(Character.TYPE, NULL_CHARACTER);
    }

    private record BeanCacheKey(EntityPath<?> entity, Class<?> type) {}
    private record RecordCacheKey(EntityPath<?> entity, Class<?> type, boolean missingFieldToNull) {}

    private static final ConcurrentMap<BeanCacheKey, Expression<?>[]> beansExpressionCache = new ConcurrentReferenceHashMap<>();
    private static final ConcurrentMap<RecordCacheKey, Expression<?>[]> recordExpressionCache = new ConcurrentReferenceHashMap<>();

    /**
     * 为给定类型和Q类创建setter调用投影所需的表达式
     * @param type 投影类型
     * @param entity Q类实例
     * @return 表达式数组
     */
    public static <T> Expression<?>[] generateSetterExpressions(
            Class<? extends T> type, EntityPath<?> entity) {
        var cacheKey = new BeanCacheKey(entity, type);
        var cache = beansExpressionCache.get(cacheKey);
        if (cache != null) return cache;

        var entityFieldMap = getEntityFieldMap(entity);
        List<Expression<?>> expressions = new ArrayList<>();
        var propertyDescriptor = ReflectUtils.getBeanSetters(type);
        for (PropertyDescriptor descriptor : propertyDescriptor) {
            Expression<?> expression = entityFieldMap.get(descriptor.getName());
            if (expression != null) expressions.add(expression);
        }

        var expressionsArray = expressions.toArray(EMPTY_EXPRESSIONS);
        beansExpressionCache.put(cacheKey, expressionsArray);
        return expressionsArray;
    }

    /**
     * 为给定Record类型和Q类创建构造调用投影所需的表达式
     *
     * @param type 投影类型
     * @param entity  Q类实例
     * @return 表达式数组
     */
    public static <T extends Record> Expression<?>[] generateRecordConstructorExpressions(
            Class<T> type, EntityPath<?> entity, boolean missingFieldToNull) {
        var cacheKey = new RecordCacheKey(entity, type, missingFieldToNull);
        var cache = recordExpressionCache.get(cacheKey);
        if (cache != null) return cache;

        var entityFieldMap = getEntityFieldMap(entity);
        RecordComponent[] recordComponents = type.getRecordComponents();
        List<Expression<?>> expressions = new ArrayList<>(recordComponents.length);

        for (RecordComponent recordComponent : recordComponents) {
            Expression<?> expression = entityFieldMap.get(recordComponent.getName());
            if (expression != null) {
                expressions.add(expression);
            } else {
                if (missingFieldToNull) {
                    var componentType = recordComponent.getType();
                    if (componentType == String.class) {
                        expressions.add(NULL_STRING);
                    } else if (componentType.isPrimitive() || PrimitiveUtils.isWrapperType(componentType)) {
                        expressions.add(primitivesNullExpressions.get(componentType));
                    } else if (componentType == BigDecimal.class) {
                        expressions.add(NULL_BIG_DECIMAL);
                    } else if (componentType == BigInteger.class) {
                        expressions.add(NULL_BIG_INTEGER);
                    } else if (componentType == Object.class) {
                        expressions.add(Expressions.nullExpression());
                    } else {
                        expressions.add(Expressions.nullExpression(componentType));
                    }
                }
            }
        }

        var expressionsArray = expressions.toArray(EMPTY_EXPRESSIONS);
        recordExpressionCache.put(cacheKey, expressionsArray);
        return expressionsArray;
    }

    private static Map<String, Expression<?>> getEntityFieldMap(EntityPath<?> entityPath) {
        Map<String, Expression<?>> fieldMap = new HashMap<>();
        ReflectionUtils.doWithLocalFields(entityPath.getClass(), field -> {
            if (Expression.class.isAssignableFrom(field.getType())) {
                try {
                    fieldMap.put(field.getName(), (Expression<?>) field.get(entityPath));
                } catch (IllegalAccessException ignored) {}
            }
        });
        return fieldMap;
    }

    public static void clearCache() {
        beansExpressionCache.clear();
        recordExpressionCache.clear();
    }

    public static <T> QBean<T> bean(Class<? extends T> type, EntityPath<?> entityPath, Expression<?>... exprs) {
        var setters = generateSetterExpressions(type, entityPath);
        if (exprs.length == 0) return Projections.bean(type, setters);
        var expressions = new Expression<?>[setters.length + exprs.length];
        System.arraycopy(setters, 0, expressions, 0, setters.length);
        System.arraycopy(exprs, 0, expressions, setters.length, exprs.length);
        return Projections.bean(type, expressions);
    }

    public static <T extends Record> ConstructorExpression<T> record(
            Class<? extends T> type, EntityPath<?> entityPath, Expression<?>... exprs) {
        if (exprs.length == 0) {
            var components = generateRecordConstructorExpressions(type, entityPath, true);
            return Projections.constructor(type, components);
        }
        var components = generateRecordConstructorExpressions(type, entityPath, false);
        var expressions = new Expression<?>[components.length + exprs.length];
        System.arraycopy(components, 0, expressions, 0, components.length);
        System.arraycopy(exprs, 0, expressions, components.length, exprs.length);
        return Projections.constructor(type, expressions);
    }

}
