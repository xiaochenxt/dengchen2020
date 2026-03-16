package io.github.dengchen2020.core.utils.bean;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.core.NativeDetector;
import org.springframework.util.ConcurrentReferenceHashMap;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING_ARRAY;

/**
 * Bean操作工具类，相比Spring的BeanUtils，会忽略null值的拷贝
 * <p>性能很高，但更推荐使用MapStruct</p>
 * <pre>{@code
 * @Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
 * public interface Convert {
 *
 *     Convert I = new ConvertImpl();
 *     void copyProperties(Params source, @MappingTarget DTO target);
 *
 * }
 * ConvertImpl类为编译期自动生成
 * 使用方式：Convert.I.copyProperties(source, target);
 * }
 * </pre>
 * @author xiaochen
 * @since 2025/1/6
 */
public abstract class BeanUtils {

    private static final ConcurrentMap<String, BeanCopier> CACHE = new ConcurrentReferenceHashMap<>();

    /**
     * 高效的bean拷贝
     * <pre>
     * 注意：
     * 源对象的属性值为null会被忽略
     * 源对象的属性需与目标对象的同名属性类型完全相同，源对象的属性需有getter方法，目标对象的同名属性需有setter方法
     * </pre>
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copyProperties(Object source, Object target) {
        if (NativeDetector.inNativeImage()) {
            org.springframework.beans.BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
            return;
        }
        copyProperties(source, target, (Converter) null);
    }

    private static BeanCopier getBeanCopier(Object source, Object target, Converter converter) {
        return getBeanCopier(source.getClass(), target.getClass(), converter);
    }

    private static BeanCopier getBeanCopier(Class<?> sourceClass, Class<?> targetClass, Converter converter) {
        boolean useConverter = converter != null;
        String cacheKey = !useConverter ? sourceClass.getName() + "@" + targetClass.getName() : sourceClass.getName() + "@" + targetClass.getName() + "@true";
        return CACHE.computeIfAbsent(cacheKey, key -> BeanCopier.create(sourceClass, targetClass, useConverter));
    }

    /**
     * 高效的bean拷贝
     * <pre>
     * 注意：
     * 源对象的属性值为null会被忽略
     * 源对象的属性需与目标对象的同名属性类型完全相同，源对象的属性需有getter方法，目标对象的同名属性需有setter方法
     * </pre>
     * @param source 源对象
     * @param target 目标对象
     * @param converter 自定义转换器
     */
    protected static void copyProperties(Object source, Object target, Converter converter) {
        if (source == null || target == null) return;
        getBeanCopier(source, target, converter).copy(source, target, converter);
    }

    /**
     * 高效的bean拷贝
     * <pre>
     * 注意：
     * 源对象的属性值为null会被忽略
     * 源对象的属性需与目标对象的同名属性类型完全相同，源对象的属性需有getter方法，目标对象的同名属性需有setter方法
     * </pre>
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 拷贝时要忽略的属性
     */
    public static void copyProperties(Object source, Object target,@NonNull String @NonNull... ignoreProperties) {
        if (NativeDetector.inNativeImage()) {
            Set<String> ignoredProperties = getNullPropertyNameSet(source);
            Collections.addAll(ignoredProperties, ignoreProperties);
            org.springframework.beans.BeanUtils.copyProperties(source, target, ignoredProperties.toArray(EMPTY_STRING_ARRAY));
            return;
        }
        copyProperties(source, target, Set.of(ignoreProperties));
    }

    /**
     * 高效的bean拷贝
     * <pre>
     * 注意：
     * 源对象的属性值为null会被忽略
     * 源对象的属性需与目标对象的同名属性类型完全相同，源对象的属性需有getter方法，目标对象的同名属性需有setter方法
     * </pre>
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreProperties 拷贝时要忽略的属性
     */
    public static void copyProperties(Object source, Object target,@NonNull Set<@NonNull String> ignoreProperties) {
        if (NativeDetector.inNativeImage()) {
            Set<String> ignoredProperties = getNullPropertyNameSet(source);
            ignoredProperties.addAll(ignoreProperties);
            org.springframework.beans.BeanUtils.copyProperties(source, target, ignoredProperties.toArray(EMPTY_STRING_ARRAY));
            return;
        }
        copyProperties(source, target, new IgnorePropertiesConverter(ignoreProperties));
    }

    private static class IgnorePropertiesConverter implements Converter {
        private final Set<String> ignoreProperties;

        public IgnorePropertiesConverter(Set<String> ignoreProperties) {
            this.ignoreProperties = ignoreProperties == null ? Collections.emptySet() : ignoreProperties;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Object convert(Object value, Class target, Object context, String setterFieldName) {
            if (ignoreProperties.contains(setterFieldName)) return null;
            return value;
        }
    }

    /**
     * 查找对象中为空的字段
     */
    public static String[] getNullPropertyNames(Object source) {
        return getNullPropertyNameSet(source).toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * 查找对象中为空的字段
     */
    public static Set<String> getNullPropertyNameSet(Object source) {
        Set<String> emptyFieldNames = new HashSet<>();
        try {
            var pds = org.springframework.beans.BeanUtils.getPropertyDescriptors(source.getClass());
            for (var pd : pds) {
                var readMethod = pd.getReadMethod();
                if (readMethod == null) continue;
                var srcValue = readMethod.invoke(source);
                if (srcValue == null) emptyFieldNames.add(pd.getName());
            }
        } catch (BeansException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("获取对象中属性为null的字段失败，class：" + source.getClass().getName(), e);
        }
        return emptyFieldNames;
    }

    /**
     * 将 source 的属性拷贝到一个新建的 Record 实例中并返回。
     * <pre>
     * 注意：
     * source 中为 null 的属性会被忽略，Record 对应组件保持零值（引用类型为 null，基本类型为 0/false）
     * source 中没有对应属性的 Record 组件保持零值
     * </pre>
     * @param source      源对象
     * @param targetClass 目标 Record 类型
     * @param <T>         Record 类型
     * @return 新建的 Record 实例
     */
    public static <T extends Record> T copyProperties(Object source, @NonNull Class<T> targetClass) {
        return copyProperties(source, targetClass, Collections.emptySet());
    }

    public static <T extends Record> T copyProperties(Object source, @NonNull Class<T> targetClass, @NonNull String @NonNull... ignoreProperties) {
        return copyProperties(source, targetClass, Set.of(ignoreProperties));
    }

    private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES = Map.of(
            boolean.class, false,
            byte.class, (byte) 0,
            short.class, (short) 0,
            int.class, 0,
            long.class, 0L,
            float.class, 0F,
            double.class, 0D,
            char.class, '\0');

    public static <T extends Record> T copyProperties(Object source, @NonNull Class<T> targetClass,@NonNull Set<@NonNull String> ignoreProperties) {
        if (source == null) return null;
        var sourceClass = source.getClass();
        var components = targetClass.getRecordComponents();
        var paramTypes = new Class<?>[components.length];
        var args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            var componentType = components[i].getType();
            paramTypes[i] = componentType;
            try {
                var pd = org.springframework.beans.BeanUtils.getPropertyDescriptor(sourceClass, components[i].getName());
                if (pd == null) {
                    if (componentType.isPrimitive()) args[i] = DEFAULT_TYPE_VALUES.get(componentType);
                    continue;
                }
                var readMethod = pd.getReadMethod();
                if (readMethod == null) {
                    if (componentType.isPrimitive()) args[i] = DEFAULT_TYPE_VALUES.get(componentType);
                    continue;
                }
                var val = readMethod.invoke(source);
                if (val != null && !ignoreProperties.contains(components[i].getName())) args[i] = val;
                else if (componentType.isPrimitive()) args[i] = DEFAULT_TYPE_VALUES.get(componentType);
            } catch (BeansException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("读取source属性失败，field：" + sourceClass.getName() + "." + components[i].getName(), e);
            }
        }
        try {
            var constructor = targetClass.getDeclaredConstructor(paramTypes);
            return constructor.newInstance(args);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("创建Record实例失败，class：" + targetClass.getName(), e);
        }
    }

}
