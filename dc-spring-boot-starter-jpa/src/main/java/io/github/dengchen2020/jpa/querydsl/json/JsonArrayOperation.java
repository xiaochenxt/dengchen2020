package io.github.dengchen2020.jpa.querydsl.json;

import tools.jackson.databind.node.ArrayNode;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

/**
 * JSON数组操作
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public interface JsonArrayOperation {

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

    JsonObjectTemplate getObject(int index);

    JsonArrayTemplate getArray(int index);

    JsonTemplate get(int index);

}
