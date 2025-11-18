package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

/**
 * cbor处理工具类
 * <p>未注入到SpringBean，与Spring的全局Jackson配置可能不一致，因此可能导致转换的部分字段数据与前者不一致</p>
 * @author xiaochen
 * @since 2025/8/29
 */
@NullMarked
public abstract class CborUtils {

    /**
     * 初始化一个{@link ObjectNode}
     *
     * @return {@link ObjectNode}
     */
    public static ObjectNode createObjectNode() {
        return CborHelper.INSTANCE.createObjectNode();
    }

    /**
     * 初始化一个{@link ArrayNode}
     *
     * @return {@link ArrayNode}
     */
    public static ArrayNode createArrayNode() {
        return CborHelper.INSTANCE.createArrayNode();
    }

    /**
     * 序列化
     *
     * @param source     对象
     * @return byte[]
     */
    public static byte[] serialize(@Nullable Object source) {
        return CborHelper.INSTANCE.serialize(source);
    }

    /**
     * 反序列化
     *
     * @param data byte[]
     * @param type   类型
     * @return T 指定类型的对象
     */
    @Nullable
    public static <T> T deserialize(byte @Nullable[] data, Class<T> type) {
        return CborHelper.INSTANCE.deserialize(data, type);
    }

    /**
     * 反序列化
     *
     * @param data byte[]
     * @param type   类型
     * @return T 指定类型的对象
     */
    @Nullable
    public static  <T> T deserialize(byte @Nullable[] data, TypeReference<T> type) {
        return CborHelper.INSTANCE.deserialize(data, type);
    }

    /**
     * 将ArrayNode转化为Stream
     * @param arrayNode ArrayNode
     * @return Stream<JsonNode>
     */
    public static Stream<JsonNode> toStream(ArrayNode arrayNode){
        return CborHelper.INSTANCE.toStream(arrayNode);
    }

}
