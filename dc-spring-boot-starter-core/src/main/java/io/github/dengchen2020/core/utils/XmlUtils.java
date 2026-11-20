package io.github.dengchen2020.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.NullMarked;

import java.util.stream.Stream;

/**
 * xml处理工具类
 * @author xiaochen
 * @since 2022/9/15
 */
@NullMarked
public abstract class XmlUtils {

    private static XmlHelper getXmlHelper() {
        return SpringUtils.applicationContext != null ? SpringUtils.applicationContext.getBean(XmlHelper.class) : XmlHelper.INSTANCE;
    }

    /**
     * 初始化一个{@link ObjectNode}
     *
     * @return {@link ObjectNode}
     */
    public static ObjectNode createObjectNode() {
        return XmlHelper.INSTANCE.createObjectNode();
    }

    /**
     * 初始化一个{@link ArrayNode}
     *
     * @return {@link ArrayNode}
     */
    public static ArrayNode createArrayNode() {
        return XmlHelper.INSTANCE.createArrayNode();
    }

    /**
     * 将xml转换为指定类型的对象
     *
     * @param xml xml
     * @param type   类型
     * @return T 指定类型的对象
     */
    public static <T> T fromXml(String xml, Class<T> type) {
        return getXmlHelper().fromXml(xml, type);
    }

    /**
     * 将xml转换为指定类型的对象
     *
     * @param xml xml
     * @return 指定类型的对象
     */
    public static <T> T fromXml(String xml, TypeReference<T> typeReference) {
        return getXmlHelper().fromXml(xml, typeReference);
    }

    /**
     * 对象转换为xml
     *
     * @param source 对象
     * @return xml
     */
    public static String toXml(Object source) {
        return getXmlHelper().toXml(source);
    }

    /**
     * 对象转换为xml，忽略null属性
     *
     * @param source 对象
     * @return xml
     */
    public static String toXmlIgnoreNull(Object source) {
        return getXmlHelper().toXmlIgnoreNull(source);
    }

    /**
     * 将xml解析为{@link JsonNode}
     *
     * @param xml xml
     * @return {@link JsonNode}
     */
    public static JsonNode readTree(String xml) {
        return getXmlHelper().readTree(xml);
    }

    /**
     * 将ArrayNode转化为Stream
     * @param arrayNode ArrayNode
     * @return {@link Stream}
     */
    public static Stream<JsonNode> toStream(ArrayNode arrayNode){
        return XmlHelper.INSTANCE.toStream(arrayNode);
    }

}
