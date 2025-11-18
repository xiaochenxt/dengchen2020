package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_BYTE_ARRAY;

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

    public CborHelper(CBORMapper cborMapper) {
        this.cborMapper = cborMapper;
        this.nonNullCborMapper = new CBORMapper.Builder(cborMapper)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
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
    public byte[] serialize(@Nullable Object source) {
        if (source == null) return EMPTY_BYTE_ARRAY;
        Class<?> clazz = source.getClass();
        if (clazz == byte[].class) {
            if (log.isDebugEnabled()) log.debug("源对象为字节数组，无需序列化");
            return (byte[]) source;
        }
        try {
            return nonNullCborMapper.writeValueAsBytes(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法序列化: " + source.getClass(), e);
        }
    }

    /**
     * 反序列化
     *
     * @param data byte[]
     * @param type   类型
     * @return T 指定类型的对象
     */
    @Nullable
    public <T> T deserialize(byte @Nullable[] data, Class<T> type) {
        if (data == null) return null;
        if (type == byte[].class) {
            if (log.isDebugEnabled()) log.debug("目标类型为字节数组，无需反序列化");
            return (T) data;
        }
        try {
            return nonNullCborMapper.readValue(data, type);
        } catch (Exception e) {
            log.error("反序列化异常，data：{}，type：{}，异常信息：", new String(data), type, e);
            return null;
        }
    }

    /**
     * 反序列化
     *
     * @param data byte[]
     * @param type   类型
     * @return T 指定类型的对象
     */
    @Nullable
    public <T> T deserialize(byte @Nullable[] data, TypeReference<T> type) {
        if (data == null) return null;
        try {
            return nonNullCborMapper.readValue(data, type);
        } catch (Exception e) {
            log.error("反序列化异常，data：{}，type：{}，异常信息：", new String(data), type, e);
            return null;
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
