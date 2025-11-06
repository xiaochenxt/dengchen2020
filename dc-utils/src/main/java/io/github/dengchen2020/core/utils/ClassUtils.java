package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class工具类，扩展Spring的{@link org.springframework.util.ClassUtils}
 * @author xiaochen
 * @since 2024/12/26
 */
public abstract class ClassUtils extends org.springframework.util.ClassUtils {

    /**
     * 获取给定类的超类的 {@link List}。
     *
     * @param cls 查找的类，可能是 {@code null}
     * @return {@link List} 的超类，如果输入null，则从这个 {@code null} 开始依次排列
     */
    public static List<Class<?>> getAllSuperclasses(@Nullable final Class<?> cls) {
        if (cls == null) return null;
        final List<Class<?>> classes = new ArrayList<>();
        Class<?> superclass = cls.getSuperclass();
        while (superclass != null) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    /**
     * 将给定类实现的所有接口作为 List 返回，
     * 包括由 superclasses 实现的 API。
     * <p>如果类本身是一个接口，则它将被作为唯一的接口返回。
     * @param clazz 类来分析接口
     * @return 给定对象作为 List 实现的所有接口
     */
    public static List<Class<?>> getAllInterfacesForClassAsList(@NonNull Class<?> clazz) {
        return getAllInterfacesForClassAsList(clazz, null);
    }

    /**
     * 将给定类实现的所有接口作为 List 返回，
     * 包括由 superclasses 实现的 API。
     * <p>如果类本身是一个接口，则它将被作为唯一的接口返回。
     * @param clazz 类来分析接口
     * @param classLoader 创建接口需要在其中可见的 ClassLoader（接受所有声明的接口时可以是 {@code null}）
     * @return 给定对象作为 List 实现的所有接口
     */
    public static List<Class<?>> getAllInterfacesForClassAsList(@NonNull Class<?> clazz, @Nullable ClassLoader classLoader) {
        if (clazz.isInterface() && isVisible(clazz, classLoader)) {
            return Collections.singletonList(clazz);
        }
        List<Class<?>> interfaces = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            Class<?>[] ifcs = current.getInterfaces();
            for (Class<?> ifc : ifcs) {
                if (isVisible(ifc, classLoader)) {
                    interfaces.add(ifc);
                }
            }
            current = current.getSuperclass();
        }
        return interfaces;
    }

}
