package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_BYTE_ARRAY;

/**
 * 简化{@link JsonMapper}常用操作的异常处理
 * @author xiaochen
 * @since 2025/11/17
 */
@NullMarked
public class JsonHelper {

    private static final Logger log = LoggerFactory.getLogger(JsonHelper.class);
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

    public JsonHelper(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
        this.nonNullJsonMapper = jsonMapper.rebuild()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
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
    @Nullable
    public String toJson(@Nullable Object source) {
        if (source == null) return null;
        try {
            return jsonMapper.writeValueAsString(source);
        } catch (Exception e) {
            log.error("toJson异常，source：{}，异常信息：", source, e);
            return null;
        }
    }

    /**
     * 将对象转换为json，忽略null属性
     *
     * @param source 源对象
     * @return json
     */
    @Nullable
    public String toJsonIgnoreNull(@Nullable Object source) {
        if (source == null) return null;
        try {
            return nonNullJsonMapper.writeValueAsString(source);
        } catch (Exception e) {
            log.error("toJsonIgnoreNull异常，source：{}，异常信息：", source, e);
            return null;
        }
    }

    /**
     * 将对象转换成指定类型的新对象
     *
     * @param source 源对象
     * @param target 类型
     * @return 指定类型的新对象
     */
    @Nullable
    public <T> T convertValue(@Nullable Object source, Class<T> target) {
        if (source == null) return null;
        try {
            return jsonMapper.convertValue(source, target);
        } catch (IllegalArgumentException e) {
            log.error("convertValue异常，source：{}，异常信息：", source, e);
            return null;
        }
    }

    /**
     * 将对象转换成指定类型的新对象
     *
     * @param source 源对象
     * @param target 类型
     * @return 指定类型的新对象
     */
    @Nullable
    public <T> T convertValue(@Nullable Object source, TypeReference<T> target) {
        if (source == null) return null;
        try {
            return jsonMapper.convertValue(source, target);
        } catch (IllegalArgumentException e) {
            log.error("convertValue异常，source：{}，异常信息：", source, e);
            return null;
        }
    }

    /**
     * 将json解析为{@link JsonNode}
     *
     * @param json json
     * @return {@link JsonNode}
     */
    @Nullable
    public JsonNode readTree(@Nullable String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return jsonMapper.readTree(json);
        } catch (Exception e) {
            log.error("readTree异常，json：{}，异常信息：", json, e);
            return null;
        }
    }

    /**
     * 将json转换为指定类型的对象
     *
     * @param json json
     * @return 指定类型的对象
     */
    @Nullable
    public <T> T fromJson(@Nullable String json, Class<T> type) {
        if (json == null || json.isBlank()) return null;
        try {
            return jsonMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("fromJson异常，json：{}，type：{}，异常信息：", json, type, e);
            return null;
        }
    }

    /**
     * 将json转换为指定类型的对象
     *
     * @param json json
     * @return 指定类型的对象
     */
    @Nullable
    public <T> T fromJson(@Nullable String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank()) return null;
        try {
            return jsonMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("fromJson异常，json：{}，typeReference：{}，异常信息：", json, typeReference, e);
            return null;
        }
    }

    /**
     * 对象转ObjectNode
     * @param source 对象
     * @return {@link ObjectNode}
     */
    @Nullable
    public ObjectNode valueToTree(Object source) {
        return jsonMapper.valueToTree(source);
    }

    /**
     * 序列化
     *
     * @param source 源对象
     * @return 字节数组
     */
    public byte[] serialize(@Nullable Object source) {
        if (source == null) return EMPTY_BYTE_ARRAY;
        Class<?> clazz = source.getClass();
        if (clazz == byte[].class) {
            if (log.isDebugEnabled()) log.debug("源对象为字节数组，无需序列化");
            return (byte[]) source;
        }
        try {
            return nonNullJsonMapper.writeValueAsBytes(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法序列化: " + source.getClass(), e);
        }
    }

    /**
     * 反序列化
     *
     * @param data 字节数组
     * @param type 反序列化数据类型
     * @return 对象
     */
    @Nullable
    public <T> T deserialize(byte @Nullable[] data, Class<T> type) {
        if (data == null) return null;
        if (type == byte[].class) {
            if (log.isDebugEnabled()) log.debug("目标类型为字节数组，无需反序列化");
            return (T) data;
        }
        try {
            return nonNullJsonMapper.readValue(data, type);
        } catch (Exception e) {
            log.error("反序列化异常，data：{}，type：{}，异常信息：", new String(data), type, e);
        }
        return null;
    }

    /**
     * 反序列化
     *
     * @param data 字节数组
     * @param type 反序列化数据类型
     * @return 对象
     */
    @Nullable
    public <T> T deserialize(byte @Nullable[] data, TypeReference<T> type) {
        if (data == null) return null;
        try {
            return nonNullJsonMapper.readValue(data, type);
        } catch (Exception e) {
            log.error("反序列化异常，data：{}，type：{}，异常信息：", new String(data), type, e);
        }
        return null;
    }

    /**
     * 反序列化，需要动态类型序列化的场景中使用，一般场景不推荐使用
     * <p>仅支持反序列化源json数据携带有{@code @class}属性或添加了注解{@code @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)}的类</p>
     *
     * @param data 字节数组
     * @return 对象
     */
    @Nullable
    public <T> T deserialize(byte @Nullable[] data) {
        if (data == null) return null;
        try {
            JsonNode tree = readTree(new String(data));
            if (tree == null) return null;
            JsonNode classNode = tree.get("@class");
            if (classNode != null) return nonNullJsonMapper.readValue(data, nonNullJsonMapper.getTypeFactory().constructFromCanonical(classNode.asText()));
        } catch (Exception e) {
            log.error("反序列化异常，data：{}，异常信息：", new String(data), e);
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

}
