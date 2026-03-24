package io.github.dengchen2020.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import java.io.IOException;

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
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        if (jp.currentToken().isStructStart()) return jp.readValueAsTree().toString();
        return jp.getValueAsString();
    }
}
