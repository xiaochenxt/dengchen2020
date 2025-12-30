package io.github.dengchen2020.jpa.querydsl.json;

import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.*;
import io.github.dengchen2020.jpa.querydsl.JpaExpressions;
import org.hibernate.type.spi.TypeConfiguration;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Json模板
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public class JsonTemplate extends StringTemplate {

    protected JsonTemplate(String template, Object... args) {
        super(TemplateFactory.DEFAULT.create(template), List.of(args));
    }

    @Override
    public <A extends Number & Comparable<? super A>> NumberExpression<A> castToNum(Class<A> type) {
        return num(type);
    }

    public <N extends Number & Comparable<?>> NumberExpression<N> num(Class<N> type) {
        return JpaExpressions.num(mixin, type);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public BooleanExpression bool() {
        return JpaExpressions.bool(mixin);
    }

    public StringExpression string() {
        return this;
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Integer> intValue() {
        return JpaExpressions.intValue(mixin);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Integer> intValue(int defaultValue) {
        return JpaExpressions.intValue(mixin).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Long> longValue() {
        return JpaExpressions.longValue(mixin);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Long> longValue(long defaultValue) {
        return JpaExpressions.longValue(mixin).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Double> doubleValue() {
        return JpaExpressions.doubleValue(mixin);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Double> doubleValue(double defaultValue) {
        return JpaExpressions.doubleValue(mixin).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Float> floatValue() {
        return JpaExpressions.floatValue(mixin);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Float> floatValue(float defaultValue) {
        return JpaExpressions.floatValue(mixin).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Short> shortValue() {
        return JpaExpressions.shortValue(mixin);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Short> shortValue(short defaultValue) {
        return JpaExpressions.shortValue(mixin).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Byte> byteValue() {
        return JpaExpressions.byteValue(mixin);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<Byte> byteValue(byte defaultValue) {
        return JpaExpressions.byteValue(mixin).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<BigDecimal> decimal() {
        return JpaExpressions.decimal(mixin);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<BigDecimal> decimal(BigDecimal defaultValue) {
        return JpaExpressions.decimal(mixin).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<BigInteger> bigInt() {
        return JpaExpressions.bigInt(mixin);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public NumberExpression<BigInteger> bigInt(BigInteger defaultValue) {
        return JpaExpressions.bigInt(mixin).coalesce(defaultValue);
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public DateTimeTemplate<LocalDateTime> dateTime() {
        return JpaExpressions.dateTime(string());
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public DateTimeTemplate<LocalDate> date() {
        return JpaExpressions.date(string());
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public TimeTemplate<LocalTime> time() {
        return JpaExpressions.time(string());
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public DateTimeTemplate<Instant> instant() {
        return JpaExpressions.instant(string());
    }

    /**
     * 类型转换详见：{@link TypeConfiguration#resolveCastTargetType(String)}
     */
    public DateTimeTemplate<Timestamp> timestamp() {
        return JpaExpressions.timestamp(string());
    }

}
