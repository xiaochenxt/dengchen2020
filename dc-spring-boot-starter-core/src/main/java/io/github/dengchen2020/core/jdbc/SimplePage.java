package io.github.dengchen2020.core.jdbc;

import java.util.List;

/**
 * 分页结果集
 *
 * @param total 总记录数
 * @param list  分页后的列表数据
 * @author xiaochen
 * @since 2019/5/29 14:16
 */
public record SimplePage<T>(Long total, List<T> list) {

}
