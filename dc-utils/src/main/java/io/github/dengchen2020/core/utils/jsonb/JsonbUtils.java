package io.github.dengchen2020.core.utils.jsonb;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

/**
 * jsonb处理工具类，在类上使用@Json注解在编译期生成适配器代码从而实现无反射的json序列化和反序列化，不支持jackson的@JsonFormat和@JsonSerialize注解
 * <p>未注入到SpringBean，与Spring的全局Jackson配置可能不一致，因此可能导致转换的部分字段数据与前者不一致</p>
 */
@NullMarked
public abstract class JsonbUtils {

    private static final Jsonb jsonb = Jsonb.builder()
            .add(LocalDateTime.class, LocalDateTimeAdapter.INSTANCE)
            .build();

    /**
     * 判断是否存在指定类型的适配器
     *
     * @param type 类型
     * @return 是否存在指定类型的适配器
     */
    public static boolean hasAdapter(Type type) {
        return jsonb.hasAdapter(type);
    }

    /**
     * 获取{@link JsonType}
     *
     * @param type 类型
     * @return {@link JsonType}
     */
    public static <T> JsonType<T> type(Class<T> type) {
        return jsonb.type(type);
    }

    /**
     * 将对象转换为json，除了java内置类型之外，都需要确保使用@Json注解生成过适配器代码
     *
     * @param source 源对象
     * @return json
     */
    public static String toJson(Object source) {
        return jsonb.toJson(source);
    }

    /**
     * 将json转换为指定类型的对象
     *
     * @param json json
     * @return 指定类型的对象
     */
    public static <T> T fromJson(String json, Class<T> type) {
        return jsonb.type(type).fromJson(json);
    }

    /**
     * 序列化，除了java内置类型之外，都需要确保使用@Json注解生成过适配器代码
     *
     * @param source 对象
     * @return 字节数组
     */
    public static byte[] serialize(Object source) {
        return jsonb.toJsonBytes(source);
    }

    /**
     * 反序列化
     *
     * @param data 字节数组
     * @param type 反序列化数据类型
     * @return 对象
     */
    public static <T> T deserialize(byte[] data, Class<T> type) {
        return jsonb.type(type).fromJson(data);
    }

}
