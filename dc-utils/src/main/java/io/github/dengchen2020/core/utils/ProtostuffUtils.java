package io.github.dengchen2020.core.utils;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.IdStrategy;
import io.protostuff.runtime.RuntimeSchema;
import java.math.BigDecimal;
import java.util.*;
import org.jspecify.annotations.NullMarked;

/**
 * Protostuff序列化和反序列化工具
 *
 * @author xiaochen
 * @since 2024/6/3
 */
@NullMarked
public abstract class ProtostuffUtils {

    public static final DefaultIdStrategy STRATEGY;

    /**
     * 将不可序列化的类型包装
     */
    private static final Set<Class<?>> WRAPPER_SET = new HashSet<>();

    /**
     * 包装类的Schema对象
     */
    private static final Schema<SerializeDeserializeWrapper> WRAPPER_SCHEMA;

    static {
        // jdk26及以上版本不再支持unsafe（使用VarHandle代替），会抛出异常，必须设置为false
        // System.setProperty("protostuff.runtime.use_sun_misc_unsafe", "false");
        STRATEGY = new DefaultIdStrategy(
                IdStrategy.DEFAULT_FLAGS | IdStrategy.MORPH_NON_FINAL_POJOS
        );
        WRAPPER_SCHEMA = RuntimeSchema.createFrom(SerializeDeserializeWrapper.class);
        WRAPPER_SET.add(List.class);
        WRAPPER_SET.add(ArrayList.class);
        WRAPPER_SET.add(HashSet.class);
        WRAPPER_SET.add(Map.class);
        WRAPPER_SET.add(HashMap.class);
        WRAPPER_SET.add(BigDecimal.class);
    }

    /**
     * 注册需要使用包装类进行序列化的Class对象
     *
     * @param clazz 对象类型
     */
    public static void registerWrapperClass(Class<?> clazz) {
        WRAPPER_SET.add(clazz);
    }

    /**
     * 序列化
     *
     * @param o 对象
     * @return 字节数组
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T o) {
        Class<T> clazz = (Class<T>) o.getClass();
        if (clazz == byte[].class) return (byte[]) o;
        //设置缓数组缓冲区
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            //判断是否是不可序列化对象，若是不能序列化对象，将对象进行包装
            if (WRAPPER_SET.contains(clazz)) {
                return ProtostuffIOUtil.toByteArray(new SerializeDeserializeWrapper<>(o), WRAPPER_SCHEMA, buffer);
            }
            return ProtostuffIOUtil.toByteArray(o, RuntimeSchema.getSchema(clazz, STRATEGY), buffer);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法序列化: " + o.getClass(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化
     *
     * @param data 字节数组
     * @param type 反序列化数据类型
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] data, Class<T> type) {
        try {
            if (type == byte[].class) return (T) data;
            //判断是否是不可序列化对象，若是不能序列化对象，将对象进行包装
            if (WRAPPER_SET.contains(type)) {
                SerializeDeserializeWrapper<T> wrapper = WRAPPER_SCHEMA.newMessage();
                ProtostuffIOUtil.mergeFrom(data, wrapper, WRAPPER_SCHEMA);
                return wrapper.getData();
            }
            Schema<T> schema = RuntimeSchema.getSchema(type, STRATEGY);
            T message = schema.newMessage();
            ProtostuffIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalArgumentException("data：" + new String(data) + "，type：" + type, e);
        }
    }

    /**
     * 序列化JAVA对象，序列化后的字节数包含类型，因此体积相对较大
     * <p>对应的反序列化：{@link ProtostuffUtils#deserializeJavaObject(byte[])}</p>
     *
     * @param source 对象
     * @return 字节数组
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serializeJavaObject(T source) {
        Class<T> clazz = (Class<T>) source.getClass();
        if (clazz == byte[].class) return (byte[]) source;
        //设置缓数组缓冲区
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtostuffIOUtil.toByteArray(new SerializeDeserializeWrapper<>(source), WRAPPER_SCHEMA, buffer);
        } catch (Exception e) {
            throw new IllegalArgumentException("source：" + source, e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化JAVA对象
     *
     * @param data 字节数组
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeJavaObject(byte[] data) {
        SerializeDeserializeWrapper<T> wrapper = WRAPPER_SCHEMA.newMessage();
        ProtostuffIOUtil.mergeFrom(data, wrapper, WRAPPER_SCHEMA);
        return wrapper.getData();
    }


    /**
     * 包装不可序列化和反序列化的类
     *
     * @param <T>
     */
    private static class SerializeDeserializeWrapper<T> {
        /**
         * 被包装的对象
         */
        private T data;

        public SerializeDeserializeWrapper(T data) {
            setData(data);
        }

        public void setData(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }

}
