package io.github.dengchen2020.core.jdbc;

/**
 * 分页查询参数
 * @author xiaochen
 * @since 2025/10/22
 */
public interface Page {

    static Page of(int page, int size){
        return of(page, size, true);
    }

    static Page of(int page, int size, boolean selectCount){
        return new PageQuery(page, size, selectCount);
    }

    /**
     * 每页数据条数，默认10；size=0，没有列表信息，只查总数total
     */
    default int size() {
        return 10;
    }

    /**
     * 页码，值>0，默认1
     */
    default int page() {
        return 1;
    }

    /**
     * 是否查询数量，不需要时设置为false（通过索引字段（例如：id）分页时可始终为false），数据量大时可显著提升查询性能
     */
    default boolean isSelectCount() {
        return true;
    }

    /**
     * 获取偏移量
     */
    default long offset() {return (long) (page() - 1) * size();}

    /**
     * @see Page#size()
     */
    default int getSize() {
        return size();
    }

    /**
     * @see Page#page()
     */
    default int getPage() {
        return page();
    }

    /**
     * @see Page#offset()
     */
    default long getOffset() {return offset();}

}
