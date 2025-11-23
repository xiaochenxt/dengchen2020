package io.github.dengchen2020.core.utils;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

/**
 * xml处理工具类
 * <p>未注入到SpringBean，与Spring的全局Jackson配置可能不一致，因此可能导致转换的部分字段数据与前者不一致</p>
 * @author xiaochen
 * @since 2022/9/15
 */
@NullMarked
public abstract class XmlUtils {

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
    @Nullable
    public static <T> T fromXml(@Nullable String xml, Class<T> type) {
        return XmlHelper.INSTANCE.fromXml(xml, type);
    }

    /**
     * 将xml转换为指定类型的对象
     *
     * @param xml xml
     * @return 指定类型的对象
     */
    @Nullable
    public static <T> T fromXml(@Nullable String xml, TypeReference<T> typeReference) {
        return XmlHelper.INSTANCE.fromXml(xml, typeReference);
    }

    /**
     * 对象转换为xml
     *
     * @param source 对象
     * @return xml
     */
    @Nullable
    public static String toXml(@Nullable Object source) {
        return XmlHelper.INSTANCE.toXml(source);
    }

    /**
     * 对象转换为xml，忽略null属性
     *
     * @param source 对象
     * @return xml
     */
    @Nullable
    public static String toXmlIgnoreNull(@Nullable Object source) {
        return XmlHelper.INSTANCE.toXmlIgnoreNull(source);
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
