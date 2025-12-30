package io.github.dengchen2020.jdbc.querydsl.json;

import tools.jackson.databind.node.ObjectNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

/**
 * JSON对象操作
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public interface JsonObjectOperation {

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param key
     * @param value
     * @return {@link BooleanExpression}
     */
    BooleanExpression contains(String key, Object value);

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    BooleanExpression contains(Map<String, Object> json);

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    BooleanExpression contains(ObjectNode json);

    /**
     * 检查JSON字段中是否存在指定key
     *
     * @param key
     * @return {@link BooleanExpression}
     */
    BooleanExpression containsKey(String key);

    JsonObjectTemplate getObject(String key);

    JsonArrayTemplate getArray(String key);

    JsonTemplate get(String key);

    JsonTemplate get(String... pathArr);

}
