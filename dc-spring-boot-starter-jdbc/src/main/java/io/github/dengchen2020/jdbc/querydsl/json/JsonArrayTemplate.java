package io.github.dengchen2020.jdbc.querydsl.json;

import tools.jackson.databind.node.ArrayNode;
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
public final class JsonArrayTemplate extends AbstarctJsonTemplate implements JsonArrayOperation {

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
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", mixin, json.toString()).isTrue();
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(Collection<?> json) {
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", mixin, JsonUtils.toJson(json)).isTrue();
    }

    public JsonObjectTemplate getObject(int index){
        return new JsonObjectTemplate("{0} -> {1}", mixin, index);
    }

    public JsonArrayTemplate getArray(int index){
        return new JsonArrayTemplate("{0} -> {1}", mixin, index);
    }

    @Override
    public JsonTemplate get(int index) {
        return new JsonTemplate("{0} ->> {1}", mixin, index);
    }
}
