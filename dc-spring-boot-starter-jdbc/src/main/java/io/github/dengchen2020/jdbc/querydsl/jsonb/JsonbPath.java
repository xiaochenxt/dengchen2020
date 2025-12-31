package io.github.dengchen2020.jdbc.querydsl.jsonb;

import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import io.github.dengchen2020.core.utils.JsonUtils;
import io.github.dengchen2020.jdbc.querydsl.json.JsonValueTemplate;
import org.jspecify.annotations.NullMarked;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.util.Collection;
import java.util.Map;

/**
 * JsonbPath，便于访问Jsonb字段，支持postgres
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public class JsonbPath<T> extends SimpleExpression<T> implements JsonbObjectOperation, JsonbArrayOperation {

    protected static final ConcurrentReferenceHashMap<Path<?>, JsonbPath<?>> cache = new ConcurrentReferenceHashMap<>(64,ConcurrentReferenceHashMap.ReferenceType.WEAK);

    protected final Path<T> pathImpl;

    public JsonbPath(Path<T> pathImpl) {
        super(pathImpl);
        this.pathImpl = pathImpl;
    }

    public static JsonbPath<?> of(Path<?> pathImpl) {
        var cached = cache.get(pathImpl);
        if (cached != null) return cached;
        final var jsonPath = new JsonbPath<>(pathImpl);
        cache.put(pathImpl, jsonPath);
        return jsonPath;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(pathImpl, context);
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(String json) {
        if (!JsonUtils.isJson(json)) throw new IllegalArgumentException(json + "is not a json");
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", pathImpl, json).isTrue();
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
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", pathImpl, JsonUtils.toJson(json)).isTrue();
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(ObjectNode json) {
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", pathImpl, json.toString()).isTrue();
    }

    @Override
    public BooleanExpression contains(ArrayNode json) {
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", pathImpl, json.toString()).isTrue();
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(Collection<?> json) {
        return Expressions.booleanTemplate("{0} @> {1}::jsonb", pathImpl, JsonUtils.toJson(json)).isTrue();
    }

    /**
     * 检查JSON字段中是否存在指定key
     *
     * @param key
     * @return {@link BooleanExpression}
     */
    public BooleanExpression containsKey(String key) {
        return Expressions.booleanTemplate("jsonb_exists({0},{1})", pathImpl, key).isTrue();
    }

    public JsonbObjectTemplate getObject(String key){
        return new JsonbObjectTemplate("{0} -> {1}", pathImpl, key);
    }

    public JsonbObjectTemplate getObject(int index){
        return new JsonbObjectTemplate("{0} -> {1}", pathImpl, index);
    }

    public JsonbArrayTemplate getArray(String key){
        return new JsonbArrayTemplate("{0} -> {1}", pathImpl, key);
    }

    public JsonbArrayTemplate getArray(int index){
        return new JsonbArrayTemplate("{0} -> {1}", pathImpl, index);
    }

    public JsonValueTemplate get(String key){
        return new JsonValueTemplate("{0} ->> {1}", pathImpl, key);
    }

    @Override
    public JsonValueTemplate get(int index) {
        return new JsonValueTemplate("{0} ->> {1}", pathImpl, index);
    }

    /**
     * 检查JSON字段中是否存在指定路径
     *
     * @param path
     * @return {@link BooleanExpression}
     */
    public BooleanExpression containsPath(String path) {
        return Expressions.booleanTemplate("jsonb_exists({0},{1})", path, path).isTrue();
    }

    public JsonValueTemplate query(String path) {
        return new JsonValueTemplate("json_value({0},{1})", pathImpl, path);
    }

    public JsonbObjectTemplate queryObject(String path){
        return new JsonbObjectTemplate("json_query({0},{1})", pathImpl, path);
    }

    public JsonbObjectTemplate getObject(String... pathArr){
        return new JsonbObjectTemplate("{0} #> {1}", pathImpl, pathArr);
    }

    public JsonValueTemplate get(String... pathArr) {
        return new JsonValueTemplate("{0} #>> {1}", pathImpl, pathArr);
    }

}
