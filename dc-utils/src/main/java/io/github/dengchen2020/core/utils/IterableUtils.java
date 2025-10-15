package io.github.dengchen2020.core.utils;

import java.util.*;

/**
 * Iterable工具类
 * @author xiaochen
 * @since 2024/12/26
 */
public abstract class IterableUtils {

    /**
     * 获取一个包含提供的可迭代对象内容的新列表
     * @param iterable 可迭代对象
     * @return {@link List}
     */
    public static <T> List<T> toList(Iterable<T> iterable) {
        if (iterable == null) return Collections.emptyList();
        if (iterable instanceof List) return (List<T>) iterable;
        return newList(iterable);
    }

    /**
     * 获取一个包含提供的可迭代对象内容的新集合
     * @param iterable 可迭代对象
     * @return {@link List}
     */
    public static <T> Collection<T> toCollection(Iterable<T> iterable) {
        if (iterable == null) return Collections.emptyList();
        if (iterable instanceof Collection<T>) return (Collection<T>) iterable;
        return newList(iterable);
    }

    private static <T> List<T> newList(Iterable<T> iterable) {
        Iterator<T> iterator = iterable.iterator();
        if (!iterator.hasNext()) return Collections.emptyList();
        List<T> list = new ArrayList<>();
        do {
            list.add(iterator.next());
        } while (iterator.hasNext());
        return list;
    }

}
