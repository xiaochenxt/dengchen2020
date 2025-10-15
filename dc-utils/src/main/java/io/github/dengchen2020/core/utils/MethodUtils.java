package io.github.dengchen2020.core.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 方法工具类
 * @author xiaochen
 * @since 2024/12/26
 */
public abstract class MethodUtils {

    /**
     * 获取使用给定注释进行注释的给定类的所有类级公共方法。
     *
     * @param cls {@link Class}查询
     * @param annotationCls {@link Annotation} 必须存在于要匹配的方法上
     * @return 一个 Method 数组（可能为空）。
     * @throws NullPointerException 如果类或注解是 {@code null}
     */
    public static Method[] getMethodsWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls) {
        return getMethodsWithAnnotation(cls, annotationCls, false, false);
    }

    /**
     * 获取给定类中用给定注释注释的所有方法。
     *
     * @param cls {@link Class}查询
     * @param annotationCls {@link Annotation} 必须存在于要匹配的方法上
     * @param searchSupers 确定是否应在给定类的整个继承层次结构中执行查找
     * @param ignoreAccess 确定是否应考虑非公共方法
     * @return 一个 Method 数组（可能为空）。
     * @throws NullPointerException 如果类或注解为 {@code null}
     */
    public static Method[] getMethodsWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls,
                                                    final boolean searchSupers, final boolean ignoreAccess) {
        return getMethodsListWithAnnotation(cls, annotationCls, searchSupers, ignoreAccess).toArray(new Method[0]);
    }

    /**
     * 获取给定类中用给定注释注释的所有方法。
     *
     * @param cls {@link Class}查询
     * @param annotationCls {@link Annotation} 必须存在于要匹配的方法上
     * @param searchSupers 确定是否应在给定类的整个继承层次结构中执行查找
     * @param ignoreAccess 确定是否应考虑非公共方法
     * @return Method 列表（可能为空）。
     * @throws NullPointerException 如果类或注释类为 {@code null}
     */
    public static List<Method> getMethodsListWithAnnotation(final Class<?> cls,
                                                            final Class<? extends Annotation> annotationCls,
                                                            final boolean searchSupers, final boolean ignoreAccess) {

        Objects.requireNonNull(cls, "cls");
        Objects.requireNonNull(annotationCls, "annotationCls");
        final List<Class<?>> classes = searchSupers ? getAllSuperclassesAndInterfaces(cls) : new ArrayList<>();
        classes.addFirst(cls);
        final List<Method> annotatedMethods = new ArrayList<>();
        classes.forEach(c -> {
            final Method[] methods = ignoreAccess ? c.getDeclaredMethods() : c.getMethods();
            Stream.of(methods).filter(method -> method.isAnnotationPresent(annotationCls)).forEachOrdered(annotatedMethods::add);
        });
        return annotatedMethods;
    }

    /**
     * 获取 {@link ClassUtils#getAllSuperclasses(Class)}和
     * {@link ClassUtils#getAllInterfacesForClassAsList(Class)}，一个来自超类，一个
     * 以广度优先的方式，依此类推。
     *
     * @param cls  要查找的类可能是 {@code null}
     * @return 按顺序组合的 {@link List} 超类和接口
     * 从这个开始上升
     * {@code null} 如果输入null
     */
    private static List<Class<?>> getAllSuperclassesAndInterfaces(final Class<?> cls) {
        if (cls == null) return null;
        final List<Class<?>> allSuperClassesAndInterfaces = new ArrayList<>();
        final List<Class<?>> allSuperclasses = ClassUtils.getAllSuperclasses(cls);
        int superClassIndex = 0;
        final List<Class<?>> allInterfaces = ClassUtils.getAllInterfacesForClassAsList(cls);
        int interfaceIndex = 0;
        while (interfaceIndex < allInterfaces.size() ||
                superClassIndex < allSuperclasses.size()) {
            final Class<?> acls;
            if (interfaceIndex >= allInterfaces.size()) {
                acls = allSuperclasses.get(superClassIndex++);
            } else if (superClassIndex >= allSuperclasses.size() || !(superClassIndex < interfaceIndex)) {
                acls = allInterfaces.get(interfaceIndex++);
            } else {
                acls = allSuperclasses.get(superClassIndex++);
            }
            allSuperClassesAndInterfaces.add(acls);
        }
        return allSuperClassesAndInterfaces;
    }

}
