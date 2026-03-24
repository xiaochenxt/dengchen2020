package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.NullMarked;

/**
 * 简化{@link XmlMapper}常用操作的异常处理
 * @author xiaochen
 * @since 2025/11/17
 */
@NullMarked
public class XmlHelper {

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
        this.nonNullXmlMapper = new XmlMapper.Builder(xmlMapper)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
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
    public String toXml(Object source) {
        try {
            return xmlMapper.writeValueAsString(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source, e);
        }
    }

    /**
     * 将对象转换为xml
     *
     * @param source 源对象
     * @return xml
     */
    public String toXmlIgnoreNull(Object source) {
        try {
            return nonNullXmlMapper.writeValueAsString(source);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source, e);
        }
    }

    /**
     * 将xml解析为{@link JsonNode}
     *
     * @param xml xml
     * @return {@link JsonNode}
     */
    public JsonNode readTree(String xml) {
        try {
            return xmlMapper.readTree(xml);
        } catch (Exception e) {
            throw new IllegalArgumentException("xml：" + xml, e);
        }
    }

    /**
     * 将xml转换为指定类型的对象
     *
     * @param xml xml
     * @return 指定类型的对象
     */
    public <T> T fromXml(String xml, Class<T> type) {
        try {
            return xmlMapper.readValue(xml, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("xml：" + xml + "，type：" + type, e);
        }
    }

    /**
     * 将xml转换为指定类型的对象
     *
     * @param xml xml
     * @return 指定类型的对象
     */
    public <T> T fromXml(String xml, TypeReference<T> typeReference) {
        try {
            return xmlMapper.readValue(xml, typeReference);
        } catch (Exception e) {
            throw new IllegalArgumentException("xml：" + xml + "，typeReference：" + typeReference, e);
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
