package io.github.dengchen2020.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.ser.jackson.RawSerializer;

/**
 * 如果值是JSON对象或数组，则将其反序列化回字符串，对应{@link RawSerializer}
 * @author xiaochen
 * @since 2026/3/23
 */
public class RawDeserializer extends StdDeserializer<String> {

    public static final RawDeserializer INSTANCE = new RawDeserializer();

    protected RawDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt) {
        if (jp.currentToken().isStructStart()) return jp.readValueAsTree().toString();
        return jp.getValueAsString();
    }
}
