package io.github.dengchen2020.jpa.querydsl.jsonb;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.dengchen2020.jpa.querydsl.json.JsonValueTemplate;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

/**
 * jsonb对象查询
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public interface JsonbObjectOperation {

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

    JsonbObjectTemplate getObject(String key);

    JsonbArrayTemplate getArray(String key);

    JsonValueTemplate get(String key);

    BooleanExpression containsPath(String path);

    JsonValueTemplate query(String path);

    JsonbObjectTemplate queryObject(String path);

    JsonbObjectTemplate getObject(String... pathArr);

    JsonValueTemplate get(String... pathArr);

}
