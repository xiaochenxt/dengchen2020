package io.github.dengchen2020.jpa.querydsl.jsonb;

import tools.jackson.databind.node.ObjectNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import io.github.dengchen2020.core.utils.JsonUtils;
import io.github.dengchen2020.jpa.querydsl.json.JsonValueTemplate;
import java.util.Collection;
import java.util.Map;
import org.jspecify.annotations.NullMarked;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING_ARRAY;

/**
 * jsonb对象查询实现
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public final class JsonbObjectTemplate extends AbstractJsonbTemplate implements JsonbObjectOperation {

    public JsonbObjectTemplate(String template, Object... args) {
        super(template, args);
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param key
     * @param value
     * @return {@link BooleanExpression}
     */
    @Override
    public BooleanExpression contains(String key, Object value) {
        return contains(Map.of(key, value));
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    @SuppressWarnings("all")
    @Override
    public BooleanExpression contains(Map<String, Object> json) {
        return containsNoCheck(JsonUtils.toJson(json));
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    @Override
    public BooleanExpression contains(ObjectNode json) {
        return containsNoCheck(json.toString());
    }

    /**
     * 检查JSON字段中是否存在指定key
     *
     * @param key
     * @return {@link BooleanExpression}
     */
    @Override
    public BooleanExpression containsKey(String key) {
        return Expressions.booleanTemplate("jsonb_exists({0},{1})", mixin, key);
    }

    @Override
    public JsonbObjectTemplate getObject(String key){
        return new JsonbObjectTemplate("jsonb_get({0},{1})", mixin, key);
    }

    @Override
    public JsonbArrayTemplate getArray(String key){
        return new JsonbArrayTemplate("jsonb_get({0},{1})", mixin, key);
    }

    @Override
    public JsonValueTemplate get(String key){
        return new JsonValueTemplate("jsonb_get_str({0},{1})", mixin, key);
    }

    @Override
    public BooleanExpression containsPath(String path) {
        return Expressions.booleanTemplate("json_exists({0},{1})", mixin, path);
    }

    @Override
    public JsonValueTemplate query(String path) {
        return new JsonValueTemplate("json_value({0},{1})", mixin, path);
    }

    @Override
    public JsonbObjectTemplate queryObject(String path) {
        return new JsonbObjectTemplate("json_query({0},{1})", mixin, path);
    }

    @Override
    public JsonbObjectTemplate getObject(String... pathArr) {
        return new JsonbObjectTemplate("jsonb_get_by_patharr({0},{1})", mixin, pathArr);
    }

    @Override
    public JsonValueTemplate get(String... pathArr) {
        return new JsonValueTemplate("jsonb_get_str_by_patharr({0},{1})", mixin, pathArr);
    }

    /**
     * 当值是对象时，设置值
     * @param pathArr
     * @param value
     * @return
     */
    @Override
    public JsonbObjectTemplate setObject(String[] pathArr, Object value) {
        return new JsonbObjectTemplate("jsonb_set({0},{1},{2})", mixin, pathArr, JsonUtils.toJson(value));
    }

    /**
     * 当值是对象时，设置值
     * @param pathArr
     * @param value
     * @return
     */
    @Override
    public JsonbObjectTemplate setObject(Collection<String> pathArr, Object value) {
        return setObject(pathArr.toArray(EMPTY_STRING_ARRAY), value);
    }

    @Override
    public JsonbObjectTemplate setObject(String path, Object value) {
        return setObject(new String[]{path}, value);
    }

    /**
     * 当值是对象时移除元素
     * @param pathArr
     * @return
     */
    @Override
    public JsonbObjectTemplate removeObject(String... pathArr) {
        return new JsonbObjectTemplate("jsonb_remove({0},{1})", mixin, pathArr);
    }

}
