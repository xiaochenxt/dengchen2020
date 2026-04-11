package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * 简化{@link JsonMapper}常用操作的异常处理
 * @author xiaochen
 * @since 2025/11/17
 */
@NullMarked
public class JsonHelper {

    private static final JsonMapper defaultJsonMapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).findAndAddModules().build();
    public static final JsonHelper INSTANCE = new JsonHelper(defaultJsonMapper);

    private final JsonMapper jsonMapper;
    private final JsonMapper nonNullJsonMapper;

    public static JsonHelper get() {
        return INSTANCE;
    }

    public JsonMapper getMapper() {
        return jsonMapper;
    }

    public JsonMapper getNonNullMapper() {
        return nonNullJsonMapper;
    }

    public JsonHelper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.nonNullJsonMapper = jsonMapper.rebuild()
                .changeDefaultPropertyInclusion(h -> h.withOverrides(JsonInclude.Value.ALL_NON_NULL))
                .build();
    }

    /**
     * 初始化一个{@link ObjectNode}
     *
     * @return {@link ObjectNode}
     */
    public ObjectNode createObjectNode() {
        return jsonMapper.createObjectNode();
    }

    /**
     * 初始化一个{@link ArrayNode}
     *
     * @return {@link ArrayNode}
     */
    public ArrayNode createArrayNode() {
        return jsonMapper.createArrayNode();
    }

    /**
     * 将对象转换为json
     *
     * @param source 源对象
     * @return json
     */
    public String toJson(Object source) {
        try {
            return jsonMapper.writeValueAsString(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source, e);
        }
    }

    /**
     * 将对象转换为json，忽略null属性
     *
     * @param source 源对象
     * @return json
     */
    public String toJsonIgnoreNull(Object source) {
        try {
            return nonNullJsonMapper.writeValueAsString(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source, e);
        }
    }

    /**
     * 将对象转换成指定类型的新对象
     *
     * @param source 源对象
     * @param target 类型
     * @return 指定类型的新对象
     */
    public <T> T convertValue(Object source, Class<T> target) {
        try {
            return jsonMapper.convertValue(source, target);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source + "，target：" + target, e);
        }
    }

    /**
     * 将对象转换成指定类型的新对象
     *
     * @param source 源对象
     * @param target 类型
     * @return 指定类型的新对象
     */
    public <T> T convertValue(Object source, TypeReference<T> target) {
        try {
            return jsonMapper.convertValue(source, target);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source + "，target：" + target, e);
        }
    }

    /**
     * 将json解析为{@link JsonNode}
     *
     * @param json json
     * @return {@link JsonNode}
     */
    public JsonNode readTree(String json) {
        try {
            return jsonMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("json：" + json, e);
        }
    }

    /**
     * 将json转换为指定类型的对象
     *
     * @param json json
     * @return 指定类型的对象
     */
    public <T> @Nullable T fromJson(String json, Class<T> type) {
        try {
            return jsonMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("json：" + json + "，type：" + type, e);
        }
    }

    /**
     * 将json转换为指定类型的对象
     *
     * @param json json
     * @return 指定类型的对象
     */
    public <T> @Nullable T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return jsonMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new IllegalArgumentException("json：" + json + "，typeReference：" + typeReference, e);
        }
    }

    /**
     * 对象转ObjectNode
     * @param source 对象
     * @return {@link ObjectNode}
     */
    public @Nullable ObjectNode valueToTree(Object source) {
        try {
            return jsonMapper.valueToTree(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source, e);
        }
    }

    /**
     * 序列化
     *
     * @param source 源对象
     * @return 字节数组
     */
    public byte[] serialize(Object source) {
        try {
            return nonNullJsonMapper.writeValueAsBytes(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source + "，type：" + source.getClass(), e);
        }
    }

    /**
     * 反序列化
     *
     * @param data 字节数组
     * @param type 反序列化数据类型
     * @return 对象
     */
    public <T> @Nullable T deserialize(byte[] data, Class<T> type) {
        if (type == byte[].class) return (T) data;
        try {
            return nonNullJsonMapper.readValue(data, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("data：" + new String(data, StandardCharsets.UTF_8) + "，type：" + type, e);
        }
    }

    /**
     * 反序列化
     *
     * @param data 字节数组
     * @param type 反序列化数据类型
     * @return 对象
     */
    public <T> @Nullable T deserialize(byte[] data, TypeReference<T> type) {
        try {
            return nonNullJsonMapper.readValue(data, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("data：" + new String(data, StandardCharsets.UTF_8) + "，type：" + type, e);
        }
    }

    /**
     * 反序列化，需要动态类型序列化的场景中使用，一般场景不推荐使用
     * <p>仅支持反序列化源json数据携带有{@code @class}属性或添加了注解{@code @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)}的类</p>
     *
     * @param data 字节数组
     * @return 对象
     */
    public <T> @Nullable T deserialize(byte[] data) {
        var dataStr = new String(data, StandardCharsets.UTF_8);
        try {
            JsonNode tree = readTree(dataStr);
            JsonNode classNode = tree.get("@class");
            if (classNode != null) return nonNullJsonMapper.readValue(data, nonNullJsonMapper.getTypeFactory().constructFromCanonical(classNode.asString()));
        } catch (Exception e) {
            throw new IllegalArgumentException("data：" + dataStr, e);
        }
        return null;
    }

    /**
     * 将ArrayNode转化为Stream
     * @param arrayNode ArrayNode
     * @return {@link Stream}
     */
    public Stream<JsonNode> toStream(ArrayNode arrayNode){
        return StreamSupport.stream(arrayNode.spliterator(),false);
    }

    /**
     * 流式校验 是否为合法的 JSON 对象（{...} 格式）或 JSON 数组（[...] 格式）（仅遍历，不构建对象），性能好
     * @param jsonStr 待校验的 JSON 字符串
     * @return 是否为合法 JSON对象或JSON数组
     */
    public boolean isJsonObjectOrArray(String jsonStr) {
        if (!StringUtils.hasText(jsonStr)) return false;
        // 去除首尾空白（避免因空格导致误判）
        String trimmed = jsonStr.trim();
        // 快速前置校验：JSON 必须以 { 或 [ 开头，以 } 或 ] 结尾
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))
                || !(trimmed.endsWith("}") || trimmed.endsWith("]"))) {
            return false;
        }
        try (JsonParser parser = jsonMapper.createParser(trimmed)) {
            // 遍历所有 token 直到结束，若过程中无异常则为合法 JSON
            while (parser.nextToken() != null) {
                // 仅遍历，不做任何处理
            }
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }

    /**
     * 流式校验是否为合法的 JSON 对象（{...} 格式）（仅遍历，不构建对象），性能好
     * @param jsonStr 待校验的 JSON 字符串
     * @return 是否为合法 JSON 对象
     */
    public boolean isJsonObject(String jsonStr) {
        if (!StringUtils.hasText(jsonStr)) return false;
        String trimmed = jsonStr.trim();
        // 前置校验：必须以 { 开头 和 } 结尾
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return false;
        try (JsonParser parser = jsonMapper.createParser(trimmed)) {
            // 校验第一个 token 是 OBJECT_START（{）
            JsonToken firstToken = parser.nextToken();
            if (firstToken != JsonToken.START_OBJECT) return false;
            // 遍历剩余 token 确保无语法错误
            while (parser.nextToken() != null) {
                // 仅遍历，不做任何处理
            }
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }

    /**
     * 流式校验是否为合法的 JSON 数组（[...] 格式）（仅遍历，不构建对象），性能好
     * @param jsonStr 待校验的 JSON 字符串
     * @return 是否为合法 JSON 数组
     */
    public boolean isJsonArray(String jsonStr) {
        if (!StringUtils.hasText(jsonStr)) return false;
        String trimmed = jsonStr.trim();
        // 前置校验：必须以 [ 开头 和 ] 结尾
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return false;
        }
        try (JsonParser parser = jsonMapper.createParser(trimmed)) {
            // 校验第一个 token 是 ARRAY_START（[）
            JsonToken firstToken = parser.nextToken();
            if (firstToken != JsonToken.START_ARRAY) return false;
            // 遍历剩余 token 确保无语法错误
            while (parser.nextToken() != null) {
                // 仅遍历，不做任何处理
            }
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }

    /**
     * 流式读取并处理json对象数据，处理完后会关闭输入流，适用于总数据量大但单个json对象数据量小的场景
     * @param inputStream 输入流
     * @param objectType 对象的数据类型
     * @param consumer 处理json数据
     */
    public <T> void readStreamHandleObject(InputStream inputStream, Class<T> objectType, Consumer<T> consumer) {
        readStream(inputStream, (parser, token) -> {
            if (token != JsonToken.START_OBJECT) return;
            try {
                consumer.accept(parser.readValueAs(objectType));
            } catch (JacksonException e) {
                throw new IllegalArgumentException("无法将json数据转换为" + objectType, e);
            }
        });
    }

    /**
     * 流式读取并处理json对象数据，处理完后会关闭输入流，适用于总数据量大但单个json对象数据量小的场景
     * @param inputStream 输入流
     * @param objectType 对象的数据类型
     * @param consumer 处理json数据
     */
    public <T> void readStreamHandleObject(InputStream inputStream, TypeReference<T> objectType, Consumer<T> consumer) {
        readStream(inputStream, (parser, token) -> {
            if (token != JsonToken.START_OBJECT) return;
            try {
                consumer.accept(parser.readValueAs(objectType));
            } catch (JacksonException e) {
                throw new IllegalArgumentException("无法将json数据转换为" + objectType, e);
            }
        });
    }

    /**
     * 流式读取并处理json对象数据，处理完后会关闭输入流，适用于总数据量大但单个json对象数据量小的场景
     * @param inputStream 输入流
     * @param consumer 处理json数据
     */
    public void readStreamHandleObject(InputStream inputStream, Consumer<ObjectNode> consumer) {
        readStreamHandleObject(inputStream, ObjectNode.class, consumer);
    }

    /**
     * 流式读取并处理json数据，处理完后会关闭输入流
     * @param inputStream 输入流
     * @param consumer 处理json数据
     * @throws IOException 输入流异常
     */
    public void readStream(InputStream inputStream, BiConsumer<JsonParser, JsonToken> consumer) {
        try (var parser = nonNullJsonMapper.createParser(inputStream)) {
            JsonToken token;
            while ((token = parser.nextToken()) != null) consumer.accept(parser, token);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("输入流错误，可能不是有效的json数据", e);
        }
    }

}
