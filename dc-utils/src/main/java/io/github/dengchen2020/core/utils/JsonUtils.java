package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

/**
 * json处理工具类
 * <p>未注入到SpringBean，与Spring的全局Jackson配置可能不一致，因此可能导致转换的部分字段数据与前者不一致</p>
 * @author xiaochen
 * @since 2022/9/15
 */
@NullMarked
public abstract class JsonUtils {

    /**
     * 初始化一个{@link ObjectNode}
     *
     * @return {@link ObjectNode}
     */
    public static ObjectNode createObjectNode() {
        return JsonHelper.INSTANCE.createObjectNode();
    }

    /**
     * 初始化一个{@link ArrayNode}
     *
     * @return {@link ArrayNode}
     */
    public static ArrayNode createArrayNode() {
        return JsonHelper.INSTANCE.createArrayNode();
    }

    /**
     * 将对象转换为json
     *
     * @param source 源对象
     * @return json
     */
    @Nullable
    public static String toJson(@Nullable Object source) {
        return JsonHelper.INSTANCE.toJson(source);
    }

    /**
     * 将对象转换为json，忽略null属性
     *
     * @param source 源对象
     * @return json
     */
    @Nullable
    public static String toJsonIgnoreNull(@Nullable Object source) {
        return JsonHelper.INSTANCE.toJsonIgnoreNull(source);
    }

    /**
     * 将对象转换成指定类型的新对象
     *
     * @param source 源对象
     * @param target 类型
     * @return 指定类型的新对象
     */
    @Nullable
    public static <T> T convertValue(@Nullable Object source, Class<T> target) {
        return JsonHelper.INSTANCE.convertValue(source, target);
    }

    /**
     * 将json解析为{@link JsonNode}
     *
     * @param json json
     * @return {@link JsonNode}
     */
    @Nullable
    public static JsonNode readTree(@Nullable String json) {
        return JsonHelper.INSTANCE.readTree(json);
    }

    /**
     * 将json转换为指定类型的对象
     *
     * @param json json
     * @return 指定类型的对象
     */
    @Nullable
    public static <T> T fromJson(@Nullable String json, Class<T> type) {
        return JsonHelper.INSTANCE.fromJson(json, type);
    }

    /**
     * 将json转换为指定类型的对象
     *
     * @param json json
     * @return 指定类型的对象
     */
    @Nullable
    public static <T> T fromJson(@Nullable String json, TypeReference<T> typeReference) {
        return JsonHelper.INSTANCE.fromJson(json, typeReference);
    }

    /**
     * 对象转ObjectNode
     * @param source 对象
     * @return {@link ObjectNode}
     */
    @Nullable
    public static ObjectNode valueToTree(Object source) {
        return JsonHelper.INSTANCE.valueToTree(source);
    }

    /**
     * 序列化
     *
     * @param source 对象
     * @return 字节数组
     */
    public static byte[] serialize(@Nullable Object source) {
        return JsonHelper.INSTANCE.serialize(source);
    }

    /**
     * 反序列化
     *
     * @param data 字节数组
     * @param type 反序列化数据类型
     * @return 对象
     */
    @Nullable
    public static <T> T deserialize(byte @Nullable[] data, Class<T> type) {
        return JsonHelper.INSTANCE.deserialize(data, type);
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
        return JsonHelper.INSTANCE.deserialize(data, type);
    }

    /**
     * 反序列化，需要动态类型序列化的场景中使用，一般场景不推荐使用
     * <p>仅支持反序列化源json数据携带有{@code @class}属性或添加了注解{@code @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)}的类</p>
     *
     * @param data 字节数组
     * @return 对象
     */
    @Nullable
    public static <T> T deserialize(byte @Nullable[] data) {
        return JsonHelper.INSTANCE.deserialize(data);
    }

    /**
     * 将ArrayNode转化为Stream
     * @param arrayNode ArrayNode
     * @return {@link Stream}
     */
    public static Stream<JsonNode> toStream(ArrayNode arrayNode){
        return JsonHelper.INSTANCE.toStream(arrayNode);
    }

}
