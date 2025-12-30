package io.github.dengchen2020.jpa.querydsl.mysql;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.jspecify.annotations.NullMarked;

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
    public static BooleanExpression findInSet(StringPath stringPath, String value) {
        return com.querydsl.core.types.dsl.Expressions.numberTemplate(Integer.class, "find_in_set({0}, {1})", stringPath, value).gt(0);
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param stringPath
     * @param value
     * @return
     */
    public static BooleanExpression findInSet(StringPath stringPath, String... value) {
        return findInSet(stringPath, String.join(",", value));
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param stringPath
     * @param value
     * @return
     */
    public static BooleanExpression findInSet(StringPath stringPath, Collection<String> value) {
        return findInSet(stringPath, String.join(",", value));
    }

    /**
     * 检查JSON字段中是否包含特定值
     * @param stringPath
     * @param value
     * @return
     */
    public static BooleanExpression jsonContains(StringPath stringPath, Number value) {
        return com.querydsl.core.types.dsl.Expressions.numberTemplate(Integer.class, "json_contains({0}, {1})", stringPath, value.toString()).gt(0);
    }

    /**
     * 检查JSON字段中是否包含特定值
     * @param stringPath
     * @param value
     * @return
     */
    public static BooleanExpression jsonContains(StringPath stringPath, String value) {
        return com.querydsl.core.types.dsl.Expressions.numberTemplate(Integer.class, "json_contains({0}, {1})", stringPath, "\"" + value + "\"").gt(0);
    }

    /**
     * 检查JSON字段中是否包含特定值
     * @param stringPath
     * @param numberPath
     * @return
     */
    public static BooleanExpression jsonContains(StringPath stringPath, NumberPath<?> numberPath) {
        return com.querydsl.core.types.dsl.Expressions.numberTemplate(Integer.class, "json_contains({0}, {1})", stringPath, numberPath.stringValue()).gt(0);
    }

    /**
     * 检查JSON字段中是否包含特定值
     * @param stringPath
     * @param stringPath2
     * @return
     */
    public static BooleanExpression jsonContains(StringPath stringPath, StringPath stringPath2) {
        return com.querydsl.core.types.dsl.Expressions.numberTemplate(Integer.class, "json_contains({0}, {1})", stringPath, stringPath2.stringValue().prepend("\"").append("\"")).gt(0);
    }

    /**
     * 检查JSON对象字段中是否包含特定值，示例：jsonContains(details, "5G", "$.features")
     * @param stringExpression
     * @param value
     * @param path
     * @return
     */
    public static BooleanExpression jsonContains(StringExpression stringExpression, String value, String path) {
        return com.querydsl.core.types.dsl.Expressions.numberTemplate(Integer.class, "json_contains({0}, {1}, {2})", stringExpression, "\"" + value + "\"", path).gt(0);
    }

    /**
     * 提取JSON字段中的值，示例：{@code jsonExtract(path, "$.name")}
     *
     * @param stringPath
     * @param value
     * @return
     */
    public static StringExpression jsonExtract(StringPath stringPath, String value) {
        return com.querydsl.core.types.dsl.Expressions.stringTemplate("json_unquote(json_extract({0}, {1}))", stringPath, com.querydsl.core.types.dsl.Expressions.stringPath("'" + value + "'")).concat("");
    }

}
