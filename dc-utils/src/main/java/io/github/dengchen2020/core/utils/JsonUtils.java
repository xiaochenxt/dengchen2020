package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * json处理工具类
 * <p>未注入到SpringBean，与Spring的全局Jackson配置可能不一致，因此可能导致转换的部分字段数据与前者不一致</p>
 * @author xiaochen
 * @since 2022/9/15
 */
public abstract class JsonUtils {

    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    protected static JsonMapper defaultObjectMapper;

    protected static ObjectMapper nonNullObjectMapper;

    static final byte[] EMPTY_ARRAY = new byte[0];

    static {
        defaultObjectMapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).addModule(new JavaTimeModule()).build();
        nonNullObjectMapper = defaultObjectMapper.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper getObjectMapper() {
        return defaultObjectMapper;
    }

    /**
     * 初始化一个{@link ObjectNode}
     *
     * @return {@link ObjectNode}
     */
    public static ObjectNode createObjectNode() {
        return defaultObjectMapper.createObjectNode();
    }

    /**
     * 初始化一个{@link ArrayNode}
     *
     * @return {@link ArrayNode}
     */
    public static ArrayNode createArrayNode() {
        return defaultObjectMapper.createArrayNode();
    }

    /**
     * 将对象转换为json
     *
     * @param src 源对象
     * @return json
     */
    @Nullable
    public static String toJson(Object src) {
        return toJson(src, false);
    }

    /**
     * 将对象转换为json
     *
     * @param src 源对象
     * @param nonNull 为true则忽略值为null的属性
     * @return json
     */
    @Nullable
    public static String toJson(Object src, boolean nonNull) {
        if (src == null) return null;
        try {
            return nonNull ? nonNullObjectMapper.writeValueAsString(src) : defaultObjectMapper.writeValueAsString(src);
        } catch (Exception e) {
            log.error("toJson异常，src：{}，nonNull：{}，信息：", src, nonNull, e);
            return null;
        }
    }

    /**
     * 将对象转换为json
     *
     * @param src 源对象
     * @param nonNull 为true则忽略值为null的属性
     * @param filterProvider 过滤器，需要实体类上添加{@code @JsonFilter}才能生效
     * @return json
     */
    @Nullable
    public static String toJson(Object src, boolean nonNull, FilterProvider filterProvider) {
        if (src == null) return null;
        try {
            return nonNull ? nonNullObjectMapper.writer(filterProvider).writeValueAsString(src) : defaultObjectMapper.writer(filterProvider).writeValueAsString(src);
        } catch (Exception e) {
            log.error("toJson异常，src：{}，nonNull：{}，信息：", src, nonNull, e);
            return null;
        }
    }

    public static FilterProvider createIncludesFilterProvider(String... includes) {
        SimpleFilterProvider provider = new SimpleFilterProvider();
        if (includes != null && includes.length > 0) provider.setDefaultFilter(SimpleBeanPropertyFilter.filterOutAllExcept(includes));
        provider.setFailOnUnknownId(false);
        return provider;
    }

    public static FilterProvider createExcludesFilterProvider(String... excludes) {
        SimpleFilterProvider provider = new SimpleFilterProvider();
        if (excludes != null && excludes.length > 0) provider.setDefaultFilter(SimpleBeanPropertyFilter.serializeAllExcept(excludes));
        provider.setFailOnUnknownId(false);
        return provider;
    }

    /**
     * 将对象转换成指定类型的新对象
     *
     * @param source 源对象
     * @param target 类型
     * @return 指定类型的新对象
     */
    @Nullable
    public static <T> T convertValue(Object source, Class<T> target) {
        if (source == null || target == null) return null;
        return defaultObjectMapper.convertValue(source, target);
    }

    /**
     * 将json解析为{@link JsonNode}
     *
     * @param json json
     * @return {@link JsonNode}
     */
    @Nullable
    public static JsonNode readTree(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return defaultObjectMapper.readTree(json);
        } catch (Exception e) {
            log.error("readTree异常，json：{}，信息：", json, e);
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
    public static <T> T fromJson(String json, Class<T> type) {
        if (json == null || json.isBlank() || type == null) return null;
        try {
            return defaultObjectMapper.readValue(json, type);
        } catch (Exception e) {
            log.error("fromJson异常，json：{}，type：{}，信息：", json, type, e);
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
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isBlank() || typeReference == null) return null;
        try {
            return defaultObjectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("fromJson异常，json：{}，typeReference：{}，信息：", json, typeReference, e);
            return null;
        }
    }

    /**
     * 对象转ObjectNode
     * @param src 对象
     * @return {@link ObjectNode}
     */
    @Nullable
    public static ObjectNode valueToTree(Object src) {
        return defaultObjectMapper.valueToTree(src);
    }

    /**
     * 序列化
     *
     * @param o 对象
     * @return 字节数组
     */
    public static byte[] serialize(Object o) {
        return serialize(o, null);
    }

    /**
     * 序列化
     *
     * @param o 对象
     * @return 字节数组
     */
    public static byte[] serialize(Object o, FilterProvider filterProvider) {
        if (o == null) return EMPTY_ARRAY;
        Class<?> clazz = o.getClass();
        if (clazz == byte[].class) {
            if (log.isDebugEnabled()) log.debug("源对象为字节数组，无需序列化");
            return (byte[]) o;
        }
        try {
            return filterProvider == null ? nonNullObjectMapper.writeValueAsBytes(o) : nonNullObjectMapper.writer(filterProvider).writeValueAsBytes(o);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法序列化: " + o.getClass(), e);
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
    public static <T> T deserialize(byte[] data, Class<T> type) {
        if (type == byte[].class) {
            if (log.isDebugEnabled()) log.debug("目标类型为字节数组，无需反序列化");
            return (T) data;
        }
        try {
            return nonNullObjectMapper.readValue(data, type);
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
    public static <T> T deserialize(byte[] data) {
        try {
            JsonNode tree = readTree(new String(data));
            if (tree == null) return null;
            JsonNode classNode = tree.get("@class");
            if (classNode != null) return nonNullObjectMapper.readValue(data, nonNullObjectMapper.getTypeFactory().constructFromCanonical(classNode.asText()));
        } catch (Exception e) {
            log.error("反序列化异常，data：{}，异常信息：", new String(data), e);
        }
        return null;
    }

    /**
     * 将ArrayNode转化为Stream
     * @param arrayNode ArrayNode
     * @return Stream<JsonNode>
     */
    public static Stream<JsonNode> toStream(ArrayNode arrayNode){
        return StreamSupport.stream(arrayNode.spliterator(),false);
    }

}
