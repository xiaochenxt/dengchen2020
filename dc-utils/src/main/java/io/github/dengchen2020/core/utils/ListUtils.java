package io.github.dengchen2020.core.utils;

import java.util.AbstractList;
import java.util.List;

/**
 * List工具类
 * @author xiaochen
 * @since 2024/12/26
 */
public abstract class ListUtils {

    /**
     * 返回连续 {@link List#subList(int, int) 子列表} 的
     * 列表，每个列表的大小相同（最终列表可能会更小）。例如
     * 对包含 {@code [a, b, c, d, e]} with a partition
     * 3 个产量的大小 {@code [[a, b, c], [d, e]]} -- 包含
     * 两个内部列表，其中包含三个元素和两个元素，全部按原始顺序排列。
     * <p>
     * 外部列表不可修改，但反映了
     * 源列表。内部列表是原始列表的子列表视图，
     * 按需生产 {@link List#subList(int, int)}，并且是
     * 对于该API中解释的有关修改的所有常见警告。
     * <p>
     * 改编自 <a href="http://code.google.com/p/guava-libraries/">guava-libraries</a>
     *
     * @param <T> 元素类型
     * @param list  返回的连续子列表
     * @param size  每个子列表的所需大小（最后一个子列表可能更小）
     * @return 连续子列表的列表
     * @throws NullPointerException 如果list为null
     * @throws IllegalArgumentException 如果size不是严格意义上的正数
     */
    public static <T> List<List<T>> partition(final List<T> list, final int size) {
        if (list == null) {
            throw new NullPointerException("List must not be null");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        return new Partition<>(list, size);
    }

    /**
     * 提供一个分割视图 {@link List}.
     */
    private static class Partition<T> extends AbstractList<List<T>> {
        private final List<T> list;
        private final int size;

        private Partition(final List<T> list, final int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public List<T> get(final int index) {
            final int listSize = size();
            if (index < 0) {
                throw new IndexOutOfBoundsException("Index " + index + " must not be negative");
            }
            if (index >= listSize) {
                throw new IndexOutOfBoundsException("Index " + index + " must be less than size " +
                        listSize);
            }
            final int start = index * size;
            final int end = Math.min(start + size, list.size());
            return list.subList(start, end);
        }

        @Override
        public int size() {
            return (int) Math.ceil((double) list.size() / (double) size);
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }
    }

}
