package io.github.dengchen2020.jpa.querydsl;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.hibernate.type.spi.TypeConfiguration;
import org.jspecify.annotations.NullMarked;

/**
 * 用于JPA的通用querydsl表达式
 * @author xiaochen
 * @since 2025/12/29
 */
@NullMarked
public final class JpaExpressions {

    private JpaExpressions(){}

    /**
     * json_value函数，用于从json字段中提取标量值（字符串|数字|布尔）
     * @param expr json字段值
     * @param path 路径
     */
    public static StringExpression jsonValue(StringExpression expr, String path) {
        return Expressions.stringTemplate("json_value({0},{1})", expr, path);
    }

    /**
     * json_query函数，用于从json字段中提取非标量值（json片段，字符串）
     * @param expr json字段值
     * @param path 路径
     */
    public static StringExpression jsonQuery(StringExpression expr, String path) {
        return Expressions.stringTemplate("json_query({0},{1})", expr, path);
    }

    /**
     * json_exists函数，判断json中是否包含指定路径
     * @param expr json字段值
     * @param path 路径
     */
    public static BooleanExpression jsonExists(StringExpression expr, String path) {
        return Expressions.booleanTemplate("json_exists({0},{1})", expr, path);
    }

    /**
     * 将日期时间转日期，datetime转date
     *
     * @param dateTimePath
     */
    public static DateExpression<LocalDate> date(DateTimePath<LocalDateTime> dateTimePath) {
        return Expressions.dateTemplate(LocalDate.class, "date({0})", dateTimePath);
    }

    public static  <N extends Number & Comparable<?>> NumberExpression<N> num(Expression<?> expr, Class<N> type) {
        var casted = Expressions.numberOperation(type, Ops.NUMCAST, expr, ConstantImpl.create(type));
        if (type.isPrimitive()) return Expressions.numberTemplate(type, "coalesce({0},0)", casted, 0);
        return casted;
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static BooleanExpression bool(Expression<?> expr) {
        return Expressions.booleanTemplate( "cast({0} as boolean)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static StringExpression string(Expression<?> expr) {
        return Expressions.stringTemplate("cast({0} as string)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Integer> intValue(Expression<?> expr) {
        return Expressions.numberTemplate(Integer.class, "cast({0} as integer)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Integer> intValue(Expression<?> expr, int defaultValue) {
        return Expressions.numberTemplate(Integer.class, "cast({0} as integer)", expr).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Long> longValue(Expression<?> expr) {
        return Expressions.numberTemplate(Long.class, "cast({0} as long)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Long> longValue(Expression<?> expr, long defaultValue) {
        return Expressions.numberTemplate(Long.class, "cast({0} as long)", expr).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Double> doubleValue(Expression<?> expr) {
        return Expressions.numberTemplate(Double.class, "cast({0} as double)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Double> doubleValue(Expression<?> expr, double defaultValue) {
        return Expressions.numberTemplate(Double.class, "cast({0} as double)", expr).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Float> floatValue(Expression<?> expr) {
        return Expressions.numberTemplate(Float.class, "cast({0} as float)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Float> floatValue(Expression<?> expr, float defaultValue) {
        return Expressions.numberTemplate(Float.class, "cast({0} as float)", expr).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Short> shortValue(Expression<?> expr) {
        return Expressions.numberTemplate(Short.class, "cast({0} as short)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Short> shortValue(Expression<?> expr, short defaultValue) {
        return Expressions.numberTemplate(Short.class, "cast({0} as short)", expr).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Byte> byteValue(Expression<?> expr) {
        return Expressions.numberTemplate(Byte.class, "cast({0} as byte)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<Byte> byteValue(Expression<?> expr, byte defaultValue) {
        return Expressions.numberTemplate(Byte.class, "cast({0} as byte)", expr).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<BigDecimal> decimal(Expression<?> expr) {
        return Expressions.numberTemplate(BigDecimal.class, "cast({0} as bigdecimal)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<BigDecimal> decimal(Expression<?> expr, BigDecimal defaultValue) {
        return Expressions.numberTemplate(BigDecimal.class, "cast({0} as bigdecimal)", expr).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<BigInteger> bigInt(Expression<?> expr) {
        return Expressions.numberTemplate(BigInteger.class, "cast({0} as biginteger)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static NumberExpression<BigInteger> bigInt(Expression<?> expr, BigInteger defaultValue) {
        return Expressions.numberTemplate(BigInteger.class, "cast({0} as biginteger)", expr).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static DateTimeTemplate<LocalDateTime> dateTime(StringExpression expr) {
        return Expressions.dateTimeTemplate(LocalDateTime.class, "cast({0} as localdatetime)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static DateTimeTemplate<LocalDate> date(StringExpression expr) {
        return Expressions.dateTimeTemplate(LocalDate.class, "cast({0} as localdate)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static TimeTemplate<LocalTime> time(StringExpression expr) {
        return Expressions.timeTemplate(LocalTime.class,"cast({0} as localtime)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static DateTimeTemplate<Instant> instant(StringExpression expr) {
        return Expressions.dateTimeTemplate(Instant.class, "cast({0} as instant)", expr);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public static DateTimeTemplate<Timestamp> timestamp(StringExpression expr) {
        return Expressions.dateTimeTemplate(Timestamp.class, "cast({0} as timestamp)", expr);
    }

}
