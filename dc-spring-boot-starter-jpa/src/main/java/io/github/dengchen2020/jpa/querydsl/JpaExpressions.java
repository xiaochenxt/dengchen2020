package io.github.dengchen2020.jpa.querydsl;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import org.hibernate.type.spi.TypeConfiguration;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于JPA的通用querydsl表达式
 * @author xiaochen
 * @since 2025/12/29
 */
@NullMarked
public final class JpaExpressions {

    private JpaExpressions(){}

    /**
     * 检查,分割存储的字符串字段中是否包含特定值，不推荐使用，推荐使用json存储，查询更方便且性能更好
     *
     * @param expr
     * @param values
     * @return {@link Predicate}
     */
    @Deprecated
    public static BooleanExpression findInSet(Expression<String> expr, boolean withQuote, Object... values) {
        if (values.length == 0) return Expressions.asBoolean(Boolean.FALSE);
        List<BooleanExpression> list = new ArrayList<>();
        String wrapTemplate = withQuote ? "concat(',\"',{0},'\",')" : "concat(',',{0},',')";
        for (var val : values) {
            var singleExpr = Expressions.booleanTemplate("position({0} in concat(',',{1},',')) > 0", Expressions.stringTemplate(wrapTemplate, val), expr);
            list.add(singleExpr);
        }
        BooleanExpression finalExpr = list.getFirst();
        for (int i = 1; i < list.size(); i++) {
            finalExpr = finalExpr.and(list.get(i));
        }
        return finalExpr;
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值，不推荐使用，推荐使用json存储，查询更方便且性能更好
     *
     * @param expr
     * @param value
     * @return {@link Predicate}
     */
    @Deprecated
    public static BooleanExpression findInSet(StringExpression expr, String... value) {
        return findInSet(expr, true, (Object[]) value);
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值，不推荐使用，推荐使用json存储，查询更方便且性能更好
     *
     * @param expr
     * @param value
     * @return {@link Predicate}
     */
    @Deprecated
    public static BooleanExpression findInSet(StringExpression expr, Number... value) {
        return findInSet(expr, false, (Object[]) value);
    }

    /**
     * 随机数
     */
    public static NumberExpression<Double> random() {
        return NumberExpression.random();
    }

    /**
     * 将日期时间转日期，datetime转date
     *
     * @param dateTimePath
     * @return
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
