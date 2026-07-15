package io.github.dengchen2020.jackson;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * 处理{@link JsonRawValue}注解
 * @author xiaochen
 * @since 2026/3/23
 */
class JsonRawValueAnnotationIntrospector extends NopAnnotationIntrospector {

    protected static final JsonRawValueAnnotationIntrospector INSTANCE = new JsonRawValueAnnotationIntrospector();

    @Override
    public Object findDeserializer(Annotated annotated) {
        JsonRawValue annotation = _findAnnotation(annotated, JsonRawValue.class);
        if (annotation != null && annotation.value()) return RawDeserializer.INSTANCE;
        return super.findDeserializer(annotated);
    }

}
