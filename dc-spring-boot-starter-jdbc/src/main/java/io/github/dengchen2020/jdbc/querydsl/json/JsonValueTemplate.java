package io.github.dengchen2020.jdbc.querydsl.json;

import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.*;
import io.github.dengchen2020.jdbc.querydsl.SqlExpressions;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * json值
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public class JsonValueTemplate extends StringTemplate {

    public JsonValueTemplate(String template, Object... args) {
        super(TemplateFactory.DEFAULT.create(template), List.of(args));
    }

    @Override
    public <A extends Number & Comparable<? super A>> NumberExpression<A> castToNum(Class<A> type) {
        return num(type);
    }

    public <N extends Number & Comparable<?>> NumberExpression<N> num(Class<N> type) {
        return SqlExpressions.num(mixin, type);
    }

    public BooleanExpression bool() {
        return Expressions.booleanTemplate( "cast({0} as boolean)", mixin);
    }

    public JsonValueTemplate string() {
        return this;
    }

    public NumberExpression<Integer> intValue() {
        return SqlExpressions.intValue(mixin);
    }

    public NumberExpression<Integer> intValue(int defaultValue) {
        return SqlExpressions.intValue(mixin).coalesce(defaultValue);
    }

    public NumberExpression<Long> longValue() {
        return SqlExpressions.longValue(mixin);
    }

    public NumberExpression<Long> longValue(long defaultValue) {
        return SqlExpressions.longValue(mixin).coalesce(defaultValue);
    }

    public NumberExpression<Double> doubleValue() {
        return SqlExpressions.doubleValue(mixin);
    }

    public NumberExpression<Double> doubleValue(double defaultValue) {
        return SqlExpressions.doubleValue(mixin).coalesce(defaultValue);
    }

    public NumberExpression<Float> floatValue() {
        return SqlExpressions.floatValue(mixin);
    }

    public NumberExpression<Float> floatValue(float defaultValue) {
        return SqlExpressions.floatValue(mixin).coalesce(defaultValue);
    }

    public NumberExpression<Short> shortValue() {
        return SqlExpressions.shortValue(mixin);
    }

    public NumberExpression<Short> shortValue(short defaultValue) {
        return SqlExpressions.shortValue(mixin).coalesce(defaultValue);
    }

    public NumberExpression<Byte> byteValue() {
        return SqlExpressions.byteValue(mixin);
    }

    public NumberExpression<Byte> byteValue(byte defaultValue) {
        return SqlExpressions.byteValue(mixin).coalesce(defaultValue);
    }

    public NumberExpression<BigDecimal> decimal() {
        return SqlExpressions.decimal(mixin);
    }

    public NumberExpression<BigDecimal> decimal(BigDecimal defaultValue) {
        return SqlExpressions.decimal(mixin).coalesce(defaultValue);
    }

    public NumberExpression<BigInteger> bigInt() {
        return SqlExpressions.bigInt(mixin);
    }

    public NumberExpression<BigInteger> bigInt(BigInteger defaultValue) {
        return SqlExpressions.bigInt(mixin).coalesce(defaultValue);
    }

    public DateExpression<LocalDate> date() {
        return Expressions.dateTemplate(LocalDate.class,"to_date({0},'YYYY-MM-DD'))", string());
    }

    public DateExpression<LocalDate> date(String pattern) {
        return Expressions.dateTemplate(LocalDate.class,"to_date({0},{1})", string(), pattern);
    }

    public DateTimeExpression<LocalDateTime> dateTime() {
        return Expressions.dateTimeTemplate(LocalDateTime.class,"to_timestamp({0},'YYYY-MM-DD HH24:MI:SS')", string());
    }

    public DateTimeExpression<LocalDateTime> dateTime(String pattern) {
        return Expressions.dateTimeTemplate(LocalDateTime.class,"to_timestamp({0},{1})", string(), pattern);
    }

    public TimeExpression<LocalTime> time() {
        return Expressions.timeTemplate(LocalTime.class,"cast({0} as time)", string());
    }

}
