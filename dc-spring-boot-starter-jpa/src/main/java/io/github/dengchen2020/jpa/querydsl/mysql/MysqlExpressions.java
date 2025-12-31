package io.github.dengchen2020.jpa.querydsl.mysql;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import io.github.dengchen2020.jpa.querydsl.JpaExpressions;
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
     * @param expr
     * @param value
     * @return
     */
    public static BooleanExpression findInSet(StringExpression expr, String value) {
        return Expressions.numberTemplate(Integer.class, "find_in_set({0}, {1})", expr, value).gt(0);
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param expr
     * @param value
     * @return
     */
    public static BooleanExpression findInSet(StringExpression expr, String... value) {
        return findInSet(expr, String.join(",", value));
    }

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     * @param expr
     * @param value
     * @return
     */
    public static BooleanExpression findInSet(StringExpression expr, Collection<String> value) {
        return findInSet(expr, String.join(",", value));
    }

    /**
     * 提取json字段中的值，示例：{@code json_column ->> '$.name'}
     *
     * @param expr json字段
     * @param path 路径
     */
    public static StringExpression jsonExtract(StringExpression expr, String path) {
        return JpaExpressions.jsonValue(expr, path);
    }

}
