package io.github.dengchen2020.core.utils.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import org.jspecify.annotations.NullMarked;

import java.time.LocalDate;
import java.util.Collection;

/**
 * 扩充querydsl的专用于mysql的sql表达式
 * @author xiaochen
 * @since 2024/4/2
 */
@NullMarked
public final class MysqlExpressions {

    private MysqlExpressions() {}

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param stringPath
     * @param value
     * @return
     */
    public static Predicate findInSet(StringPath stringPath, String value) {
        return Expressions.numberTemplate(Integer.class, "find_in_set({0}, {1})", stringPath, value).gt(0);
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param stringPath
     * @param value
     * @return
     */
    public static Predicate findInSet(StringPath stringPath, String... value) {
        return findInSet(stringPath, String.join(",", value));
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param stringPath
     * @param value
     * @return
     */
    public static Predicate findInSet(StringPath stringPath, Collection<String> value) {
        return findInSet(stringPath, String.join(",", value));
    }

    /**
     * 随机数
     * @return
     */
    public static StringPath rand() {
        return Expressions.stringPath("rand()");
    }

    /**
     * 检查JSON字段中是否包含特定值
     * @param stringPath
     * @param value
     * @return
     */
    public static Predicate jsonContains(StringPath stringPath, Number value) {
        return Expressions.numberTemplate(Integer.class, "json_contains({0}, {1})", stringPath, value.toString()).gt(0);
    }

    /**
     * 检查JSON字段中是否包含特定值
     * @param stringPath
     * @param value
     * @return
     */
    public static Predicate jsonContains(StringPath stringPath, String value) {
        return Expressions.numberTemplate(Integer.class, "json_contains({0}, {1})", stringPath, "\"" + value + "\"").gt(0);
    }

    /**
     * 检查JSON字段中是否包含特定值
     * @param stringPath
     * @param numberPath
     * @return
     */
    public static Predicate jsonContains(StringPath stringPath, NumberPath<?> numberPath) {
        return Expressions.numberTemplate(Integer.class, "json_contains({0}, {1})", stringPath, numberPath.stringValue()).gt(0);
    }

    /**
     * 检查JSON字段中是否包含特定值
     * @param stringPath
     * @param stringPath2
     * @return
     */
    public static Predicate jsonContains(StringPath stringPath, StringPath stringPath2) {
        return Expressions.numberTemplate(Integer.class, "json_contains({0}, {1})", stringPath, stringPath2.stringValue().prepend("\"").append("\"")).gt(0);
    }

    /**
     * 检查JSON对象字段中是否包含特定值，示例：jsonContains(details, "5G", "$.features")
     * @param stringExpression
     * @param value
     * @param path
     * @return
     */
    public static Predicate jsonContains(StringExpression stringExpression, String value, String path) {
        return Expressions.numberTemplate(Integer.class, "json_contains({0}, {1}, {2})", stringExpression, "\"" + value + "\"", path).gt(0);
    }

    /**
     * 提取JSON字段中的值，示例：{@code jsonExtract(path, "$.name")}
     *
     * @param stringPath
     * @param value
     * @return
     */
    public static StringExpression jsonExtract(StringPath stringPath, String value) {
        return Expressions.stringTemplate("json_unquote(json_extract({0}, {1}))", stringPath, Expressions.stringPath("'" + value + "'")).concat("");
    }

    /**
     * 将日期时间转日期，datetime转date
     * @param dateTimePath
     * @return
     */
    public static DateExpression<LocalDate> date(DateTimePath<java.time.LocalDateTime> dateTimePath) {
        return Expressions.dateTemplate(LocalDate.class, "date({0})", dateTimePath);
    }

}
