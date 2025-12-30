package io.github.dengchen2020.jdbc.querydsl;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.*;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 扩充sql表达式
 *
 * @author xiaochen
 * @since 2024/4/2
 */
@NullMarked
public final class SqlExpressions {

    private SqlExpressions(){}

    /**
     * 随机数，部分数据库支持，例如mysql
     */
    public static NumberExpression<Double> rand() {
        return Expressions.numberTemplate(Double.class, "rand()");
    }

    /**
     * 随机数，部分数据库支持，例如postgres
     */
    public static NumberExpression<Double> random() {
        return NumberExpression.random();
    }

    public static  <N extends Number & Comparable<?>> NumberExpression<N> num(Expression<?> expr, Class<N> type) {
        var casted = Expressions.numberOperation(type, Ops.NUMCAST, expr, ConstantImpl.create(type));
        if (type.isPrimitive()) return Expressions.numberTemplate(type, "coalesce({0},0)", casted, 0);
        return casted;
    }

    public static  <N extends Number & Comparable<?>> NumberExpression<N> num(Expression<?> expr, Class<N> type, N defaultValue) {
        return Expressions.numberOperation(type, Ops.NUMCAST, expr, ConstantImpl.create(type)).coalesce(defaultValue);
    }

    public static StringExpression string(Expression<?> expr) {
        return Expressions.stringOperation(Ops.STRING_CAST, expr);
    }

    public static NumberExpression<Integer> intValue(Expression<?> expr) {
        return num(expr, Integer.class);
    }

    public static NumberExpression<Integer> intValue(Expression<?> expr, int defaultValue) {
        return num(expr, Integer.class, defaultValue);
    }

    public static NumberExpression<Long> longValue(Expression<?> expr) {
        return num(expr, Long.class);
    }

    public static NumberExpression<Long> longValue(Expression<?> expr, long defaultValue) {
        return num(expr, Long.class, defaultValue);
    }

    public static NumberExpression<Double> doubleValue(Expression<?> expr) {
        return num(expr, Double.class);
    }

    public static NumberExpression<Double> doubleValue(Expression<?> expr, double defaultValue) {
        return num(expr, Double.class, defaultValue);
    }

    public static NumberExpression<Float> floatValue(Expression<?> expr) {
        return num(expr, Float.class);
    }

    public static NumberExpression<Float> floatValue(Expression<?> expr, float defaultValue) {
        return num(expr, Float.class, defaultValue);
    }

    public static NumberExpression<Short> shortValue(Expression<?> expr) {
        return num(expr, Short.class);
    }

    public static NumberExpression<Short> shortValue(Expression<?> expr, short defaultValue) {
        return num(expr, Short.class, defaultValue);
    }

    public static NumberExpression<Byte> byteValue(Expression<?> expr) {
        return num(expr, Byte.class);
    }

    public static NumberExpression<Byte> byteValue(Expression<?> expr, byte defaultValue) {
        return num(expr, Byte.class, defaultValue);
    }

    public static NumberExpression<BigDecimal> decimal(Expression<?> expr) {
        return num(expr, BigDecimal.class);
    }

    public static NumberExpression<BigDecimal> decimal(Expression<?> expr, BigDecimal defaultValue) {
        return num(expr, BigDecimal.class, defaultValue);
    }

    public static NumberExpression<BigInteger> bigInt(Expression<?> expr) {
        return num(expr, BigInteger.class);
    }

    public static NumberExpression<BigInteger> bigInt(Expression<?> expr, BigInteger defaultValue) {
        return num(expr, BigInteger.class, defaultValue);
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

}
