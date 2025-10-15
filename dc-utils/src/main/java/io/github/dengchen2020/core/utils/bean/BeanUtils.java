package io.github.dengchen2020.core.utils.bean;

import org.jspecify.annotations.NonNull;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.core.NativeDetector;
import org.springframework.util.ConcurrentReferenceHashMap;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

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
    public static void copyProperties(Object source, Object target,@NonNull String... ignoreProperties) {
        if (NativeDetector.inNativeImage()) {
            Set<String> ignoredProperties = getNullPropertyNameSet(source);
            Collections.addAll(ignoredProperties, ignoreProperties);
            org.springframework.beans.BeanUtils.copyProperties(source, target, ignoredProperties.toArray(new String[0]));
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
    public static void copyProperties(Object source, Object target, Set<String> ignoreProperties) {
        if (NativeDetector.inNativeImage()) {
            Set<String> ignoredProperties = getNullPropertyNameSet(source);
            ignoredProperties.addAll(ignoreProperties);
            org.springframework.beans.BeanUtils.copyProperties(source, target, ignoredProperties.toArray(new String[0]));
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
        return getNullPropertyNameSet(source).toArray(new String[0]);
    }

    /**
     * 查找对象中为空的字段
     */
    public static Set<String> getNullPropertyNameSet(Object source) {
        java.beans.PropertyDescriptor[] pds = ReflectUtils.getBeanProperties(source.getClass());
        Set<String> emptyFieldNames = new HashSet<>();
        try {
            for (java.beans.PropertyDescriptor pd : pds) {
                Object srcValue =  pd.getReadMethod().invoke(source);
                if (srcValue == null) emptyFieldNames.add(pd.getName());
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return emptyFieldNames;
    }

}
