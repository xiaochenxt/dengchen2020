package io.github.dengchen2020.jdbc.querydsl.json;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import io.github.dengchen2020.core.utils.JsonUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Collection;
import java.util.Map;

/**
 * JsonPath，便于访问Jsonb字段
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public class JsonPath<T> extends SimpleExpression<T> implements JsonObjectOperation, JsonArrayOperation {

    protected static final ConcurrentReferenceHashMap<Path<?>, JsonPath<?>> cache = new ConcurrentReferenceHashMap<>(64,ConcurrentReferenceHashMap.ReferenceType.WEAK);

    protected final Path<T> path;

    public JsonPath(Path<T> path) {
        super(path);
        this.path = path;
    }

    public static JsonPath<?> of(Path<?> path) {
        var cached = cache.get(path);
        if (cached != null) return cached;
        var jsonPath = new JsonPath<>(path);
        cache.put(path, jsonPath);
        return jsonPath;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(path, context);
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(String json) {
        if (!JsonUtils.isJson(json)) throw new IllegalArgumentException(json + "is not a json");
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", path, json).isTrue();
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
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", path, JsonUtils.toJson(json)).isTrue();
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(ObjectNode json) {
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", path, json.toString()).isTrue();
    }

    @Override
    public BooleanExpression contains(ArrayNode json) {
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", path, json.toString()).isTrue();
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(Collection<?> json) {
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", path, JsonUtils.toJson(json)).isTrue();
    }

    /**
     * 检查JSON字段中是否存在指定key
     *
     * @param key
     * @return {@link BooleanExpression}
     */
    public BooleanExpression containsKey(String key) {
        return Expressions.booleanTemplate("jsonb_exists({0},{1})", path, key).isTrue();
    }

    public JsonObjectTemplate getObject(String key){
        return new JsonObjectTemplate("{0} -> {1}", path, key);
    }

    public JsonObjectTemplate getObject(int index){
        return new JsonObjectTemplate("{0} -> {1}", path, index);
    }

    public JsonArrayTemplate getArray(String key){
        return new JsonArrayTemplate("{0} -> {1}", path, key);
    }

    public JsonArrayTemplate getArray(int index){
        return new JsonArrayTemplate("{0} -> {1}", path, index);
    }

    public JsonTemplate get(String key){
        return new JsonTemplate("{0} ->> {1}", path, key);
    }

    @Override
    public JsonTemplate get(int index) {
        return new JsonTemplate("{0} ->> {1}", path, index);
    }

    public JsonObjectTemplate getObject(String... pathArr){
        return new JsonObjectTemplate("{0} #> {1}", path, pathArr);
    }

    public JsonTemplate get(String... pathArr) {
        return new JsonTemplate("{0} #>> {1}", path, pathArr);
    }
}
