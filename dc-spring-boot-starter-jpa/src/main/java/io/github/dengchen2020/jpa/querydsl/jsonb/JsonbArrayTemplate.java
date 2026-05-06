package io.github.dengchen2020.jpa.querydsl.jsonb;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.dengchen2020.core.utils.JsonUtils;
import io.github.dengchen2020.jpa.querydsl.json.JsonValueTemplate;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING_ARRAY;

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
        return containsNoCheck(json.toString());
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    @SuppressWarnings("all")
    @Override
    public BooleanExpression contains(Collection<?> json) {
        return containsNoCheck(JsonUtils.toJson(json));
    }

    @Override
    public JsonbObjectTemplate getObject(int index){
        return new JsonbObjectTemplate("jsonb_get({0},{1})", mixin, index);
    }

    @Override
    public JsonbArrayTemplate getArray(int index){
        return new JsonbArrayTemplate("jsonb_get({0},{1})", mixin, index);
    }

    @Override
    public JsonValueTemplate get(int index) {
        return new JsonValueTemplate("jsonb_get_str({0},{1})", mixin, index);
    }

    /**
     * 当值是数组时，设置值
     * @param pathArr
     * @param value
     * @return
     */
    @Override
    public JsonbArrayTemplate setArray(String[] pathArr, Object value) {
        return new JsonbArrayTemplate("jsonb_set({0},{1},{2})", mixin, pathArr, JsonUtils.toJson(value));
    }

    /**
     * 当值是数组时，设置值
     * @param pathArr
     * @param value
     * @return
     */
    @Override
    public JsonbArrayTemplate setArray(Collection<String> pathArr, Object value) {
        return setArray(pathArr.toArray(EMPTY_STRING_ARRAY), value);
    }

    @Override
    public JsonbArrayTemplate setArray(String path, Object value) {
        return setArray(new String[]{path}, value);
    }

    /**
     * 当值是数组时移除元素
     * @param pathArr
     * @return
     */
    @Override
    public JsonbArrayTemplate removeArray(String... pathArr) {
        return new JsonbArrayTemplate("jsonb_remove({0},{1})", mixin, pathArr);
    }
}
