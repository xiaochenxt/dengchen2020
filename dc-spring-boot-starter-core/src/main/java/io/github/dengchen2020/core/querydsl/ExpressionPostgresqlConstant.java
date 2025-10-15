package io.github.dengchen2020.core.querydsl;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import io.github.dengchen2020.core.utils.JsonUtils;

import java.time.LocalDate;
import java.util.Collection;

/**
 * 扩充querydsl的sql表达式
 *
 * @author xiaochen
 * @since 2024/4/2
 */
public class ExpressionPostgresqlConstant {

    /**
     * 检查,分割存储的字符串字段中是否包含特定值
     *
     * @param stringPath
     * @param value
     * @return
     */
    public static Predicate findInSet(StringPath stringPath, String value) {
        return Expressions.stringTemplate("function('array_position',function('string_to_array',{0},','),{1})", stringPath, value).isNotNull();
    }

    /**
     * 随机数
     *
     * @return
     */
    public static StringPath rand() {
        return Expressions.stringPath("random()");
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param stringPath
     * @param value
     * @return
     */
    public static Predicate jsonContains(StringPath stringPath, Number... value) {
        return Expressions.booleanTemplate("sql('? @> ?::jsonb',{0},{1})", stringPath, JsonUtils.toJson(value)).eq(Boolean.TRUE);
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param stringPath
     * @param value
     * @return
     */
    public static Predicate jsonContains(StringPath stringPath, Collection<Number> value) {
        return Expressions.booleanTemplate("sql('? @> ?::jsonb',{0},{1})", stringPath, JsonUtils.toJson(value)).eq(Boolean.TRUE);
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param jsonb
     * @param key
     * @return
     */
    public static Predicate jsonContains(StringExpression jsonb, String... key) {
        return Expressions.booleanTemplate("sql('? @> ?::jsonb',{0},{1})", jsonb, JsonUtils.toJson(key)).eq(Boolean.TRUE);
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param jsonb
     * @param key
     * @return
     */
    public static Predicate jsonContains(StringExpression jsonb, Collection<String> key) {
        return Expressions.booleanTemplate("sql('? @> ?::jsonb',{0},{1})", jsonb, JsonUtils.toJson(key)).eq(Boolean.TRUE);
    }

    /**
     * 检查JSON对象字段中是否包含特定值，示例：jsonb_contains(jsonb_extract_path(combo_menu,'spValueIds'),'"kG54"')
     *
     * @param jsonb
     * @param value
     * @param key
     * @return
     */
    public static Predicate jsonContains(StringExpression jsonb, String value, String key) {
        return jsonExtractString(jsonb, key).eq(value);
    }

    public static StringTemplate jsonExtractString(StringExpression jsonb, String key){
        return Expressions.stringTemplate("sql('? ->> ?',{0},{1})", jsonb, Expressions.asString(key));
    }

    public static NumberExpression<Long> jsonExtractLong(StringExpression jsonb, String key){
        return jsonExtractString(jsonb, key).castToNum(Long.class);
    }

    public static NumberExpression<Integer> jsonExtractInteger(StringExpression jsonb, String key){
        return jsonExtractString(jsonb, key).castToNum(Integer.class);
    }

    public static StringTemplate jsonExtract(StringExpression jsonb, String key){
        return Expressions.stringTemplate("sql('? -> ?',{0},{1})", jsonb, Expressions.asString(key));
    }

    public static StringTemplate jsonExtract(StringExpression jsonb, int index){
        return Expressions.stringTemplate("sql('? -> ?',{0},{1})", jsonb, index);
    }

    /**
     * 将日期时间转日期，datetime转date
     *
     * @param dateTimePath
     * @return
     */
    public static DateExpression<LocalDate> date(DateTimePath<java.time.LocalDateTime> dateTimePath) {
        return Expressions.dateTemplate(LocalDate.class, "date({0})", dateTimePath);
    }

}
