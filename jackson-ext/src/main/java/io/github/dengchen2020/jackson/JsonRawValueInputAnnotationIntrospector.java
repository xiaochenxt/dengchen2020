package io.github.dengchen2020.jackson;

import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.NopAnnotationIntrospector;
import tools.jackson.databind.ser.jackson.RawSerializer;

/**
 * JsonRawValueInput注解的处理
 * @author xiaochen
 * @since 2026/3/23
 */
class JsonRawValueInputAnnotationIntrospector extends NopAnnotationIntrospector {

    protected static final JsonRawValueInputAnnotationIntrospector INSTANCE = new JsonRawValueInputAnnotationIntrospector();

    protected static final RawSerializer<String> rawSerializer = new RawSerializer<>(String.class);

    @Override
    public Object findDeserializer(MapperConfig<?> config, Annotated am) {
        JsonRawValue annotation = _findAnnotation(am, JsonRawValue.class);
        if (annotation != null && annotation.value()) return RawDeserializer.INSTANCE;
        return super.findDeserializer(config, am);
    }

    @Override
    public Object findSerializer(MapperConfig<?> config, Annotated am) {
        JsonRawValue annotation = _findAnnotation(am, JsonRawValue.class);
        if (annotation != null && annotation.value()) return rawSerializer;
        return super.findSerializer(config, am);
    }
}
