package io.github.dengchen2020.core.jdbc;

import java.util.List;

/**
 * 统计分页报表
 *
 * @param total     总记录数
 * @param list      分页后的列表数据
 * @param totalData 总数据
 * @author xiaochen
 * @since 2023/7/25
 */
public record StatsPage<T>(Long total, List<T> list, Object totalData) {

    public StatsPage(Long total, List<T> list) {
        this(total, list, null);
    }

    public StatsPage(SimplePage<T> simplePage, Object totalData) {
        this(simplePage.total(), simplePage.list(), totalData);
    }

}
