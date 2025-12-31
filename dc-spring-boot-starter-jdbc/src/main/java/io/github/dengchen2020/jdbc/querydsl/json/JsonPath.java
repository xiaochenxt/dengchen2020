package io.github.dengchen2020.jdbc.querydsl.json;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import org.jspecify.annotations.NullMarked;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * JsonPath，便于访问Json字段，支持大部分数据库
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public class JsonPath<T> extends SimpleExpression<T> {

    protected static final ConcurrentReferenceHashMap<Path<?>, JsonPath<?>> cache = new ConcurrentReferenceHashMap<>(64,ConcurrentReferenceHashMap.ReferenceType.WEAK);

    protected final Path<T> pathImpl;

    public JsonPath(Path<T> pathImpl) {
        super(pathImpl);
        this.pathImpl = pathImpl;
    }

    public static JsonPath<?> of(Path<?> pathImpl) {
        var cached = cache.get(pathImpl);
        if (cached != null) return cached;
        final var jsonPath = new JsonPath<>(pathImpl);
        cache.put(pathImpl, jsonPath);
        return jsonPath;
    }

    @Override
    public <R, C> R accept(Visitor<R, C> v, C context) {
        return v.visit(pathImpl, context);
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

    public JsonValueTemplate queryObject(String path){
        return new JsonValueTemplate("json_query({0},{1})", pathImpl, path);
    }

}
