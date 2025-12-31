package io.github.dengchen2020.jdbc.querydsl.jsonb;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import io.github.dengchen2020.jdbc.querydsl.json.JsonValueTemplate;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

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

}
