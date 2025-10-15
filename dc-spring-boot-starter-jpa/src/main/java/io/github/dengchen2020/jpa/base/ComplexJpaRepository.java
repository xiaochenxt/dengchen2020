package io.github.dengchen2020.jpa.base;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import io.github.dengchen2020.core.jdbc.PageParam;
import io.github.dengchen2020.core.jdbc.SimplePage;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.stream.Stream;

/**
 * 复杂CRUD的jpa操作接口
 * @author xiaochen
 * @since 2024/6/18
 */
@NoRepositoryBean
public interface ComplexJpaRepository<T> {

    /**
     * Querydsl的Q类实例
     * @return {@link EntityPath}
     */
    EntityPath<T> path();

    /**
     * Querydsl的Q类实例对应的路径构造器
     * @return {@link EntityPath}
     */
    PathBuilder<T> builder();

    /**
     * Querydsl分页查询
     * @param query JPAQuery<R>
     * @param param 分页参数
     * @param o 排序方式
     * @return SimplePage<R>
     */
    <R> SimplePage<R> fetchPage(JPAQuery<R> query, PageParam param, OrderSpecifier<?>... o);

    /**
     * Querydsl分页条件查询
     *
     * @param param 分页参数
     * @param o     排序方式
     * @return 分页后的数据
     */
    SimplePage<T> findAll(Predicate predicate, PageParam param, OrderSpecifier<?>... o);

    /**
     * 返回流读取器
     * @param query JPAQuery<R>
     * @param param 分页参数
     * @param o 排序方式
     * @return Stream<R>
     */
    <R> Stream<R> fetchStream(JPAQuery<R> query, PageParam param, OrderSpecifier<?>... o);

    /**
     * 返回流读取器
     * @param predicate 条件
     * @param param 分页参数
     * @param o 排序方式
     * @return Stream<T>
     */
    Stream<T> findStream(Predicate predicate, PageParam param, OrderSpecifier<?>... o);

    /**
     * 返回流读取器
     * @param param 分页参数
     * @param o 排序方式
     * @return Stream<T>
     */
    Stream<T> findStream(PageParam param, OrderSpecifier<?>... o);

    /**
     * 返回流读取器
     * @param o 排序方式
     * @return Stream<T>
     */
    Stream<T> findStream(OrderSpecifier<?>... o);

}
