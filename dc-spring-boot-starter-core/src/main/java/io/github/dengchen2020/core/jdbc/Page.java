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

    static Page of(int page, int size, boolean isSelectCount){
        return new PageQuery(page, size, isSelectCount);
    }

    /**
     * 页码，值>0
     */
    int page();

    /**
     * 每页数据条数，值>=0，size=0，没有列表信息，只查总数total
     */
    int size();

    /**
     * 获取偏移量
     */
    default long offset() {
        return (long) (page() - 1) * size();
    }

    /**
     * 是否查询数量，不需要时设置为false（通过索引字段（例如：id）分页时可始终为false），数据量大时可显著提升查询性能
     */
    boolean isSelectCount();

}
