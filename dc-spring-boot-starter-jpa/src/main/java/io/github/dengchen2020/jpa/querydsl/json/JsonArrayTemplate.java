package io.github.dengchen2020.jpa.querydsl.json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import io.github.dengchen2020.core.utils.JsonUtils;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

/**
 * Json模板
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public final class JsonArrayTemplate extends AbstartJsonTemplate implements JsonArrayOperation {

    public JsonArrayTemplate(String template, Object... args) {
        super(template, args);
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(ArrayNode json) {
        return Expressions.booleanTemplate("sql('? @> ?::jsonb',{0},{1})", mixin, json.toString()).isTrue();
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(Collection<?> json) {
        return Expressions.booleanTemplate("sql('? @> ?::jsonb',{0},{1})", mixin, JsonUtils.toJson(json)).isTrue();
    }

    public JsonObjectTemplate getObject(int index){
        return new JsonObjectTemplate("sql('? -> ?',{0},{1})", mixin, index);
    }

    public JsonArrayTemplate getArray(int index){
        return new JsonArrayTemplate("sql('? -> ?',{0},{1})", mixin, index);
    }

    @Override
    public JsonTemplate get(int index) {
        return new JsonTemplate("jsonb_get_str({0},{1})", mixin, index);
    }
}
