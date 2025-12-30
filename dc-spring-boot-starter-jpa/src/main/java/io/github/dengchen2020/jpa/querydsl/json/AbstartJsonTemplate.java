package io.github.dengchen2020.jpa.querydsl.json;

import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DslTemplate;
import com.querydsl.core.types.dsl.Expressions;
import io.github.dengchen2020.core.utils.JsonUtils;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Json模板
 * @author xiaochen
 * @since 2025/12/28
 */
@NullMarked
public sealed class AbstartJsonTemplate extends DslTemplate<String> permits JsonObjectTemplate, JsonArrayTemplate {

    protected AbstartJsonTemplate(String template, Object... args) {
        super(String.class,TemplateFactory.DEFAULT.create(template), List.of(args));
    }

    /**
     * 检查JSON字段中是否包含特定值
     *
     * @param json
     * @return {@link BooleanExpression}
     */
    public BooleanExpression contains(String json) {
        if (!JsonUtils.isJson(json)) throw new IllegalArgumentException(json + "is not a json");
        return Expressions.booleanTemplate("sql('? @> ?::jsonb',{0},{1})", mixin, json).isTrue();
    }

}
