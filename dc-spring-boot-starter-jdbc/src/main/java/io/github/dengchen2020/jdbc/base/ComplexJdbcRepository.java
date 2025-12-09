package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.SQLQuery;
import io.github.dengchen2020.core.jdbc.Page;
import io.github.dengchen2020.core.jdbc.SimplePage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.stream.Stream;

/**
 * 复杂CRUD的jdbc操作接口
 * @author xiaochen
 * @since 2024/6/18
 */
@NullMarked
interface ComplexJdbcRepository<T> {

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
     * @return SimplePage<R>
     */
    <R> SimplePage<R> fetchPage(SQLQuery<R> query, Page page, OrderSpecifier<?>... o);

    /**
     * Querydsl分页条件查询
     *
     * @param page 分页参数
     * @param o     排序方式
     * @return 分页后的数据
     */
    SimplePage<T> findAll(Predicate predicate, Page page, OrderSpecifier<?>... o);

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源 </br>
     * @param query JPAQuery<R>
     * @param page 分页参数
     * @param o 排序方式
     * @return Stream<R>
     */
    <R> Stream<R> fetchStream(SQLQuery<R> query, @Nullable Page page, OrderSpecifier<?>... o);

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param predicate 条件
     * @param page 分页参数
     * @param o 排序方式
     * @return Stream<T>
     */
    Stream<T> findStream(Predicate predicate,@Nullable Page page, OrderSpecifier<?>... o);

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param page 分页参数
     * @param o 排序方式
     * @return Stream<T>
     */
    Stream<T> findStream(@Nullable Page page, OrderSpecifier<?>... o);

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param o 排序方式
     * @return Stream<T>
     */
    Stream<T> findStream(OrderSpecifier<?>... o);

    OrderSpecifier<?>[] EMPTY_ORDER_SPECIFIER = new OrderSpecifier<?>[0];

    /**
     * Querydsl分页查询
     * @param query JPAQuery<R>
     * @param page 分页参数
     * @return SimplePage<R>
     */
    default <R> SimplePage<R> fetchPage(SQLQuery<R> query, Page page){
        return fetchPage(query, page, EMPTY_ORDER_SPECIFIER);
    }

    /**
     * Querydsl分页条件查询
     *
     * @param page 分页参数
     * @return 分页后的数据
     */
    default SimplePage<T> findAll(Predicate predicate, Page page){
        return findAll(predicate, page, EMPTY_ORDER_SPECIFIER);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param query JPAQuery<R>
     * @param page 分页参数
     * @return Stream<R>
     */
    default <R> Stream<R> fetchStream(SQLQuery<R> query,@Nullable Page page){
        return fetchStream(query, page, EMPTY_ORDER_SPECIFIER);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param predicate 条件
     * @param page 分页参数
     * @return Stream<T>
     */
    default Stream<T> findStream(Predicate predicate,@Nullable Page page){
        return findStream(predicate, page, EMPTY_ORDER_SPECIFIER);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @param page 分页参数
     * @return Stream<T>
     */
    default Stream<T> findStream(@Nullable Page page){
        return findStream(page, EMPTY_ORDER_SPECIFIER);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @return Stream<T>
     */
    default Stream<T> findStream(){
        return findStream(EMPTY_ORDER_SPECIFIER);
    }

}
