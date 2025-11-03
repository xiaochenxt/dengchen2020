package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * cbor处理工具类
 * @author xiaochen
 * @since 2025/8/29
 */
public abstract class CborUtils {

    private static final Logger log = LoggerFactory.getLogger(CborUtils.class);

    protected static CBORMapper defaultCborMapper;

    protected static ObjectMapper nonNullCborMapper;

    static {
        defaultCborMapper = CBORMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).addModule(new JavaTimeModule()).build();
        nonNullCborMapper = defaultCborMapper.copy().setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper getCBORMapper() {
        return defaultCborMapper;
    }

    /**
     * 初始化一个{@link ObjectNode}
     *
     * @return {@link ObjectNode}
     */
    public static ObjectNode createObjectNode() {
        return defaultCborMapper.createObjectNode();
    }

    /**
     * 初始化一个{@link ArrayNode}
     *
     * @return {@link ArrayNode}
     */
    public static ArrayNode createArrayNode() {
        return defaultCborMapper.createArrayNode();
    }

    /**
     * 反序列化
     *
     * @param data byte[]
     * @param type   类型
     * @return T 指定类型的对象
     */
    @Nullable
    public static <T> T deserialize(byte[] data, Class<T> type) {
        if (data == null) return null;
        try {
            return defaultCborMapper.readValue(data, type);
        } catch (IOException e) {
            log.error("fromCbor异常，data：{}，type：{}，信息：", data, type, e);
            return null;
        }
    }

    /**
     * 序列化
     *
     * @param value 对象
     * @return byte[]
     */
    public static byte[] serialize(Object value) {
        return serialize(value, false);
    }

    /**
     * 序列化
     *
     * @param src     对象
     * @param nonNull 为true则忽略值为null的属性
     * @return byte[]
     */
    public static byte[] serialize(Object src, boolean nonNull) {
        if (src == null) return null;
        try {
            return nonNull ? nonNullCborMapper.writeValueAsBytes(src) : defaultCborMapper.writeValueAsBytes(src);
        } catch (Exception e) {
            log.error("toCbor异常，src：{}，nonNull：{}，信息：", src, nonNull, e);
            return null;
        }
    }

    /**
     * 将ArrayNode转化为Stream
     * @param arrayNode ArrayNode
     * @return Stream<JsonNode>
     */
    public static Stream<JsonNode> toStream(@NonNull ArrayNode arrayNode){
        return StreamSupport.stream(arrayNode.spliterator(),false);
    }

}
