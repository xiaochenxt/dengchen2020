package io.github.dengchen2020.core.jdbc;

/**
 * 分页查询
 * @author xiaochen
 * @since 2025/10/22
 */
record PageQuery(int page, int size, boolean isSelectCount) implements Page {

    PageQuery {
        if (page < 1) page = 1;
        if (size < 0) size = 0;
    }

    public PageQuery(int page, int size) {
        this(page, size, false);
    }

    public long getOffset() {
        return (long) (page - 1) * size;
    }

}
