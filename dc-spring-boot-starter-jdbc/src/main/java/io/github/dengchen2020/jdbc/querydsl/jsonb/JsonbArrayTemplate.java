package io.github.dengchen2020.jdbc.querydsl.jsonb;

import tools.jackson.databind.node.ArrayNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import io.github.dengchen2020.core.utils.JsonUtils;
import io.github.dengchen2020.jdbc.querydsl.json.JsonValueTemplate;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

/**
 * jsonb数组查询实现
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public final class JsonbArrayTemplate extends AbstractJsonbTemplate implements JsonbArrayOperation {

    public JsonbArrayTemplate(String template, Object... args) {
        super(template, args);
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    @Override
    public BooleanExpression contains(ArrayNode json) {
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", mixin, json.toString()).isTrue();
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    @Override
    public BooleanExpression contains(Collection<?> json) {
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", mixin, JsonUtils.toJson(json)).isTrue();
    }

    @Override
    public JsonbObjectTemplate getObject(int index){
        return new JsonbObjectTemplate("{0} -> {1}", mixin, index);
    }

    @Override
    public JsonbArrayTemplate getArray(int index){
        return new JsonbArrayTemplate("{0} -> {1}", mixin, index);
    }

    @Override
    public JsonValueTemplate get(int index) {
        return new JsonValueTemplate("{0} ->> {1}", mixin, index);
    }
}
