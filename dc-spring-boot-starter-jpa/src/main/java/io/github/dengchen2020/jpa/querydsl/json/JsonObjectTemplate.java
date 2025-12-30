package io.github.dengchen2020.jpa.querydsl.json;

import tools.jackson.databind.node.ObjectNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import io.github.dengchen2020.core.utils.JsonUtils;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

/**
 * Json模板
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public final class JsonObjectTemplate extends AbstartJsonTemplate implements JsonObjectOperation {

    public JsonObjectTemplate(String template, Object... args) {
        super(template, args);
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param key
     * @param value
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(String key, Object value) {
        return contains(Map.of(key, value));
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(Map<String, Object> json) {
        return Expressions.booleanTemplate("sql('? @> ?::jsonb',{0},{1})", mixin, JsonUtils.toJson(json)).isTrue();
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(ObjectNode json) {
        return Expressions.booleanTemplate("sql('? @> ?::jsonb',{0},{1})", mixin, json.toString()).isTrue();
    }

    /**
     * 检查JSON字段中是否存在指定key
     *
     * @param key
     * @return {@link BooleanExpression}
     */
    public BooleanExpression containsKey(String key) {
        return Expressions.booleanTemplate("sql('jsonb_exists(?,?)',{0},{1})", mixin, key).isTrue();
    }

    public JsonObjectTemplate getObject(String key){
        return new JsonObjectTemplate("sql('? -> ?',{0},{1})", mixin, key);
    }

    public JsonArrayTemplate getArray(String key){
        return new JsonArrayTemplate("sql('? -> ?',{0},{1})", mixin, key);
    }

    public JsonTemplate get(String key){
        return new JsonTemplate("jsonb_get_str({0},{1})", mixin, key);
    }

    public JsonObjectTemplate getObject(String... pathArr){
        return new JsonObjectTemplate("sql('? #> ?',{0},{1})", mixin, pathArr);
    }

    public JsonTemplate get(String... pathArr) {
        return new JsonTemplate("jsonb_get_str_by_patharr({0},{1})", mixin, pathArr);
    }

}
