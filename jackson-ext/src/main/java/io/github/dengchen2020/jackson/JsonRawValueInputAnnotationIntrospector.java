package io.github.dengchen2020.jackson;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;

/**
 * JsonRawValueInput注解的处理
 * @author xiaochen
 * @since 2026/3/23
 */
class JsonRawValueInputAnnotationIntrospector extends NopAnnotationIntrospector {

    protected static final JsonRawValueInputAnnotationIntrospector INSTANCE = new JsonRawValueInputAnnotationIntrospector();

    protected static final RawSerializer<String> rawSerializer = new RawSerializer<>(String.class);

    @Override
    public Object findDeserializer(Annotated annotated) {
        JsonRawValue annotation = _findAnnotation(annotated, JsonRawValue.class);
        if (annotation != null && annotation.value()) return RawDeserializer.INSTANCE;
        return super.findDeserializer(annotated);
    }

    @Override
    public Object findSerializer(Annotated am) {
        JsonRawValue annotation = _findAnnotation(am, JsonRawValue.class);
        if (annotation != null && annotation.value()) return rawSerializer;
        return super.findSerializer(am);
    }
}
