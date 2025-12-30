package io.github.dengchen2020.jdbc.querydsl.postgres;

import com.querydsl.core.types.dsl.*;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 扩充querydsl的专用于postgresql的sql表达式
 *
 * @author xiaochen
 * @since 2024/4/2
 */
@NullMarked
public final class PostgresExpressions {

    private PostgresExpressions(){}

    /**
     * 将日期时间转日期，datetime转date
     *
     * @param dateTimePath
     * @return
     */
    public static DateExpression<LocalDate> date(DateTimePath<java.time.LocalDateTime> dateTimePath) {
        return com.querydsl.core.types.dsl.Expressions.dateTemplate(LocalDate.class, "date({0})", dateTimePath);
    }

    public static DateExpression<LocalDate> date(StringExpression value) {
        return Expressions.dateTemplate(LocalDate.class,"to_date({0},''YYYY-MM-DD'')", value);
    }

    public static DateExpression<LocalDate> date(StringExpression value, String pattern) {
        return Expressions.dateTemplate(LocalDate.class,"to_date({0},{1}))", value, pattern);
    }

    public static DateTimeExpression<LocalDateTime> dateTime(StringExpression value) {
        return Expressions.dateTimeTemplate(LocalDateTime.class,"to_timestamp({0},''YYYY-MM-DD HH24:MI:SS''))", value);
    }

    public static DateTimeExpression<LocalDateTime> dateTime(StringExpression value, String pattern) {
        return Expressions.dateTimeTemplate(LocalDateTime.class,"to_timestamp({0},{1})", value, pattern);
    }

    public static TimeExpression<LocalTime> time(StringExpression value) {
        return Expressions.timeTemplate(LocalTime.class,"cast({0} as time)", value);
    }

}
