package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.cbor.CBORMapper;

/**
 * 简化{@link CBORMapper}常用操作的异常处理
 * @author xiaochen
 * @since 2025/11/17
 */
@NullMarked
public class CborHelper {

    private static final Logger log = LoggerFactory.getLogger(CborHelper.class);
    private static final CBORMapper defaultCborMapper = CBORMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).findAndAddModules().build();
    public static final CborHelper INSTANCE = new CborHelper(defaultCborMapper);

    protected final CBORMapper cborMapper;

    protected final CBORMapper nonNullCborMapper;

    public static CborHelper get() {
        return INSTANCE;
    }

    public CBORMapper getMapper() {
        return cborMapper;
    }

    public CBORMapper getNonNullMapper() {
        return nonNullCborMapper;
    }

    public CborHelper(CBORMapper cborMapper) {
        this.cborMapper = cborMapper;
        this.nonNullCborMapper = cborMapper.rebuild()
                .changeDefaultPropertyInclusion(h -> h.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

    /**
     * 初始化一个{@link ObjectNode}
     *
     * @return {@link ObjectNode}
     */
    public ObjectNode createObjectNode() {
        return cborMapper.createObjectNode();
    }

    /**
     * 初始化一个{@link ArrayNode}
     *
     * @return {@link ArrayNode}
     */
    public ArrayNode createArrayNode() {
        return cborMapper.createArrayNode();
    }

    /**
     * 序列化
     *
     * @param source 源对象
     * @return byte[]
     */
    public byte[] serialize(Object source) {
        try {
            return nonNullCborMapper.writeValueAsBytes(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source + "，type：" + source.getClass(), e);
        }
    }

    /**
     * 反序列化
     *
     * @param data byte[]
     * @param type   类型
     * @return T 指定类型的对象
     */
    public <T> @Nullable T deserialize(byte[] data, Class<T> type) {
        if (type == byte[].class) return (T) data;
        try {
            return nonNullCborMapper.readValue(data, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("data：" + new String(data) + "，type：" + type, e);
        }
    }

    /**
     * 反序列化
     *
     * @param data byte[]
     * @param type   类型
     * @return T 指定类型的对象
     */
    public <T> @Nullable T deserialize(byte[] data, TypeReference<T> type) {
        try {
            return nonNullCborMapper.readValue(data, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("data：" + new String(data) + "，type：" + type, e);
        }
    }

    /**
     * 将ArrayNode转化为Stream
     * @param arrayNode ArrayNode
     * @return Stream<JsonNode>
     */
    public Stream<JsonNode> toStream(ArrayNode arrayNode){
        return StreamSupport.stream(arrayNode.spliterator(),false);
    }

}
