package io.github.dengchen2020.jackson;

import com.fasterxml.jackson.annotation.JsonRawValue;
import tools.jackson.databind.cfg.MapperConfig;
import tools.jackson.databind.introspect.Annotated;
import tools.jackson.databind.introspect.NopAnnotationIntrospector;

/**
 * 处理{@link JsonRawValue}注解
 * @author xiaochen
 * @since 2026/3/23
 */
class JsonRawValueAnnotationIntrospector extends NopAnnotationIntrospector {

    protected static final JsonRawValueAnnotationIntrospector INSTANCE = new JsonRawValueAnnotationIntrospector();

    @Override
    public Object findDeserializer(MapperConfig<?> config, Annotated am) {
        JsonRawValue annotation = _findAnnotation(am, JsonRawValue.class);
        if (annotation != null && annotation.value()) return RawDeserializer.INSTANCE;
        return super.findDeserializer(config, am);
    }

}
