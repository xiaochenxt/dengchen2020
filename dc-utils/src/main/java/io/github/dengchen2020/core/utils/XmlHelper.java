package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.xml.XmlMapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 简化{@link XmlMapper}常用操作的异常处理
 * @author xiaochen
 * @since 2025/11/17
 */
@NullMarked
public class XmlHelper {

    private static final Logger log = LoggerFactory.getLogger(XmlHelper.class);
    private static final XmlMapper defaultXmlMapper = XmlMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).findAndAddModules().build();
    public static final XmlHelper INSTANCE = new XmlHelper(defaultXmlMapper);

    private final XmlMapper xmlMapper;
    private final XmlMapper nonNullXmlMapper;

    public static XmlHelper get() {
        return INSTANCE;
    }

    public XmlMapper getMapper() {
        return xmlMapper;
    }

    public XmlMapper getNonNullMapper() {
        return nonNullXmlMapper;
    }

    public XmlHelper(XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
        this.nonNullXmlMapper = xmlMapper.rebuild()
                .changeDefaultPropertyInclusion(h -> h.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

    /**
     * 初始化一个{@link ObjectNode}
     *
     * @return {@link ObjectNode}
     */
    public ObjectNode createObjectNode() {
        return xmlMapper.createObjectNode();
    }

    /**
     * 初始化一个{@link ArrayNode}
     *
     * @return {@link ArrayNode}
     */
    public ArrayNode createArrayNode() {
        return xmlMapper.createArrayNode();
    }

    /**
     * 将对象转换为xml
     *
     * @param source 源对象
     * @return xml
     */
    @Nullable
    public String toXml(@Nullable Object source) {
        if (source == null) return null;
        try {
            return xmlMapper.writeValueAsString(source);
        } catch (Exception e) {
            log.error("toXml异常，source：{}，异常信息：", source, e);
            return null;
        }
    }

    /**
     * 将对象转换为xml
     *
     * @param source 源对象
     * @return xml
     */
    @Nullable
    public String toXmlIgnoreNull(@Nullable Object source) {
        if (source == null) return null;
        try {
            return nonNullXmlMapper.writeValueAsString(source);
        } catch (Exception e) {
            log.error("toXmlIgnoreNull异常，source：{}，异常信息：", source, e);
            return null;
        }
    }

    /**
     * 将xml解析为{@link JsonNode}
     *
     * @param xml xml
     * @return {@link JsonNode}
     */
    @Nullable
    public JsonNode readTree(@Nullable String xml) {
        if (xml == null || xml.isBlank()) return null;
        try {
            return xmlMapper.readTree(xml);
        } catch (Exception e) {
            log.error("readTree异常，xml：{}，异常信息：", xml, e);
            return null;
        }
    }

    /**
     * 将xml转换为指定类型的对象
     *
     * @param xml xml
     * @return 指定类型的对象
     */
    @Nullable
    public <T> T fromXml(@Nullable String xml, Class<T> type) {
        if (xml == null || xml.isBlank()) return null;
        try {
            return xmlMapper.readValue(xml, type);
        } catch (Exception e) {
            log.error("fromXml异常，xml：{}，type：{}，异常信息：", xml, type, e);
            return null;
        }
    }

    /**
     * 将xml转换为指定类型的对象
     *
     * @param xml xml
     * @return 指定类型的对象
     */
    @Nullable
    public <T> T fromXml(@Nullable String xml, TypeReference<T> typeReference) {
        if (xml == null || xml.isBlank()) return null;
        try {
            return xmlMapper.readValue(xml, typeReference);
        } catch (Exception e) {
            log.error("fromXml异常，xml：{}，typeReference：{}，异常信息：", xml, typeReference, e);
            return null;
        }
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
