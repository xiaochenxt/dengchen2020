package io.github.dengchen2020.jpa.querydsl.jsonb;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.dengchen2020.jpa.querydsl.json.JsonValueTemplate;
import java.util.Collection;
import org.jspecify.annotations.NullMarked;

/**
 * jsonb数组查询
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public interface JsonbArrayOperation {

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    BooleanExpression contains(ArrayNode json);

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    BooleanExpression contains(Collection<?> json);

    JsonbObjectTemplate getObject(int index);

    JsonbArrayTemplate getArray(int index);

    JsonValueTemplate get(int index);

    /**
     * 当值是数组时，设置值
     * @param pathArr
     * @param value
     * @return
     */
    JsonbArrayTemplate setArray(String[] pathArr, Object value);

    /**
     * 当值是数组时，设置值
     * @param pathArr
     * @param value
     * @return
     */
    JsonbArrayTemplate setArray(Collection<String> pathArr, Object value);

    /**
     * 当值是数组时，设置值
     * @param path
     * @param value
     * @return
     */
    JsonbArrayTemplate setArray(String path, Object value);

    /**
     * 当值是数组时移除元素
     * @param pathArr
     * @return
     */
    JsonbArrayTemplate removeArray(String... pathArr);

}
