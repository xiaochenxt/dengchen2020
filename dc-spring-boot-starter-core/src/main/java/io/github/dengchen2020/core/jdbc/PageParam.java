package io.github.dengchen2020.core.jdbc;

import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.Range;

import java.util.Objects;

/**
 * 分页查询条件
 *
 * @author xiaochen
 * @since 2019/2/26 10:17
 */
public class PageParam {

    public static PageParam of(int page, int size){
        return of(page, size, true, null);
    }

    public static PageParam of(int page, int size, boolean selectCount){
        return of(page, size, selectCount, null);
    }

    public static PageParam of(int page, int size, boolean selectCount, Boolean deleted) {
        PageParam param = new PageParam();
        param.setPage(page);
        param.setSize(size);
        param.setSelectCount(selectCount);
        param.setDeleted(deleted);
        return param;
    }

    /**
     * 页码，值>0，默认1
     */
    @Min(value = 1)
    private int page = 1;

    /**
     * 每页数据条数，1-100，默认10；size=0，没有列表信息，只查总数total
     */
    @Range(min = 0, max = 100)
    private int size = 10;

    /**
     * 是否查询数量，不需要时设置为false（通过limit分页时一般只需要第一页查询数量，通过id分页时可始终为false），数据量大时可显著提升查询性能
     */
    private boolean selectCount = true;

    /**
     * 查询逻辑删除的数据，true：已删除，false：未删除
     */
    private Boolean deleted;

    public int getSize() {
        return size;
    }

    public int getPage() {
        return page;
    }

    public long getOffset() {
        return (long) (page - 1) * size;
    }

    public void setPage(int page) {
        this.page = Math.max(page, 1);
    }

    public void setSize(int size) {
        this.size = Math.max(size, 0);
    }

    public boolean isSelectCount() {
        return selectCount;
    }

    public void setSelectCount(boolean selectCount) {
        this.selectCount = selectCount;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PageParam pageParam = (PageParam) o;
        return page == pageParam.page && size == pageParam.size && selectCount == pageParam.selectCount && Objects.equals(deleted, pageParam.deleted);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, selectCount, deleted);
    }

    @Override
    public String toString() {
        return "PageParam{" +
                "page=" + page +
                ", size=" + size +
                ", selectCount=" + selectCount +
                ", deleted=" + deleted +
                '}';
    }
}
