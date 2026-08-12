package io.github.dengchen2020.core.utils.bean;

/**
 * 提供更多上下文参数以实现更多功能
 * @author xiaochen
 * @since 2025/1/6
 */
@SuppressWarnings({"rawtypes"})
public interface Converter {
    Object convert(Object value, Class target, Object context, String setterFieldName);
}
