package io.github.dengchen2020.jdbc.querydsl.mysql;

import com.querydsl.core.types.dsl.*;
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
        return Expressions.numberTemplate(Integer.class, "find_in_set({0}, {1})", stringPath, value).gt(0);
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
     * 提取json字段中的值，示例：{@code json_column ->> '$.name'}
     *
     * @param expr json字段
     * @param path 路径
     */
    public static StringExpression jsonExtract(StringExpression expr, String path) {
        return Expressions.stringTemplate("{0} ->> {1})", expr, path);
    }

    /**
     * json_value函数，用于从json字段中提取标量值（字符串|数字|布尔）
     * @param expr json字段
     * @param path 路径
     */
    public static StringExpression jsonValue(StringExpression expr, String path) {
        return Expressions.stringTemplate("json_value({0},{1})", expr, path);
    }

    /**
     * json_query函数，用于从json字段中提取非标量值（json片段，字符串）
     * @param expr json字段
     * @param path 路径
     */
    public static StringExpression jsonQuery(StringExpression expr, String path) {
        return Expressions.stringTemplate("{0} -> {1}", expr, path);
    }

    /**
     * json_exists函数，判断json中是否包含指定路径
     * @param expr json字段
     * @param path 路径
     */
    public static BooleanExpression jsonExists(StringExpression expr, String path) {
        return Expressions.booleanTemplate("json_contains_path({0},'all',{1})", expr, path);
    }

}
