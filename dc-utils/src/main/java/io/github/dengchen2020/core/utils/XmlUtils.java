package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jspecify.annotations.NonNull;
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
public abstract class XmlUtils {

    private static final Logger log = LoggerFactory.getLogger(XmlUtils.class);

    protected static XmlMapper defaultXmlMapper;

    protected static ObjectMapper nonNullXmlMapper;

    static {
        defaultXmlMapper = XmlMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).addModule(new JavaTimeModule()).build();
        nonNullXmlMapper = defaultXmlMapper.copy().setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper getXmlMapper() {
        return defaultXmlMapper;
    }

    /**
     * 初始化一个{@link ObjectNode}
     *
     * @return {@link ObjectNode}
     */
    public static ObjectNode createObjectNode() {
        return defaultXmlMapper.createObjectNode();
    }

    /**
     * 初始化一个{@link ArrayNode}
     *
     * @return {@link ArrayNode}
     */
    public static ArrayNode createArrayNode() {
        return defaultXmlMapper.createArrayNode();
    }

    /**
     * 将xml转换为指定类型的对象
     *
     * @param xml xml
     * @param type   类型
     * @return T 指定类型的对象
     */
    @Nullable
    public static <T> T fromXml(String xml, Class<T> type) {
        if (xml == null || xml.isBlank() || type == null) return null;
        try {
            return defaultXmlMapper.readValue(xml, type);
        } catch (JsonProcessingException e) {
            log.error("fromXml异常，xml：{}，type：{}，信息：", xml, type, e);
            return null;
        }
    }

    /**
     * 对象转换为xml
     *
     * @param value 对象
     * @return xml
     */
    @Nullable
    public static String toXml(Object value) {
        return toXml(value, false);
    }

    /**
     * 对象转换为xml
     *
     * @param src 对象
     * @param nonNull 为true则忽略值为null的属性
     * @return xml
     */
    @Nullable
    public static String toXml(Object src, boolean nonNull) {
        if (src == null) return null;
        try {
            return nonNull ? nonNullXmlMapper.writeValueAsString(src) : defaultXmlMapper.writeValueAsString(src);
        } catch (Exception e) {
            log.error("toXml异常，src：{}，nonNull：{}，信息：", src, nonNull, e);
            return null;
        }
    }

    /**
     * 对象转换为xml
     *
     * @param src 对象
     * @param nonNull 为true则忽略值为null的属性
     * @param filterProvider 过滤器，需要实体类上添加{@code @JsonFilter}才能生效
     * @return xml
     */
    @Nullable
    public static String toXml(Object src, boolean nonNull, FilterProvider filterProvider) {
        if (src == null) return null;
        try {
            return nonNull ? nonNullXmlMapper.writer(filterProvider).writeValueAsString(src) : defaultXmlMapper.writer(filterProvider).writeValueAsString(src);
        } catch (Exception e) {
            log.error("toXml异常，src：{}，nonNull：{}，信息：", src, nonNull, e);
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
     * 将ArrayNode转化为Stream
     * @param arrayNode ArrayNode
     * @return Stream<JsonNode>
     */
    public static Stream<JsonNode> toStream(@NonNull ArrayNode arrayNode){
        return StreamSupport.stream(arrayNode.spliterator(),false);
    }

}
