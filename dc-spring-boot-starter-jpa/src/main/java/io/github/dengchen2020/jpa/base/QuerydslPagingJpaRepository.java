package io.github.dengchen2020.jpa.base;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import io.github.dengchen2020.core.jdbc.Page;
import io.github.dengchen2020.core.jdbc.SimplePage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Querydsl分页操作接口
 * @author xiaochen
 * @since 2024/6/18
 */
@NullMarked
public interface QuerydslPagingJpaRepository<T> {

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
     * @param page 分页参数
     * @param o 排序方式
     */
    <R> SimplePage<R> fetchPage(JPAQuery<R> query, Page page, OrderSpecifier<?>... o);

    /**
     * Querydsl分页条件查询
     *
     * @param page 分页参数
     * @param o     排序方式
     * @return 分页后的数据
     */
    SimplePage<T> findAll(@Nullable Predicate predicate, Page page, OrderSpecifier<?>... o);

    /**
     * Querydsl分页条件查询
     *
     * @param page 分页参数
     * @param o 排序方式
     * @return 分页后的数据
     */
    default SimplePage<T> findAll(Page page, OrderSpecifier<?>... o){
        return findAll(null, page, o);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体存于持久上下文中，需根据实际情况处理完后清理，否则内存不能及时释放 </br>
     * @param query JPAQuery<R>
     * @param page 分页参数
     * @param o 排序方式
     */
    <R> Stream<R> fetchStream(JPAQuery<R> query, @Nullable Page page, OrderSpecifier<?>... o);

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体存于持久上下文中，需根据实际情况处理完后清理，否则内存不能及时释放 </br>
     * @param predicate 条件
     * @param page 分页参数
     * @param o 排序方式
     */
    Stream<T> findStream(@Nullable Predicate predicate,@Nullable Page page, OrderSpecifier<?>... o);

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体存于持久上下文中 </br>
     * @param page 分页参数
     * @param o 排序方式
     */
    default Stream<T> findStream(@Nullable Page page, OrderSpecifier<?>... o) {
        return findStream(null, page, o);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体存于持久上下文中，需根据实际情况处理完后清理，否则内存不能及时释放 </br>
     * @param o 排序方式
     */
    default Stream<T> findStream(OrderSpecifier<?>... o) {
        return findStream(null, null, o);
    }

}
