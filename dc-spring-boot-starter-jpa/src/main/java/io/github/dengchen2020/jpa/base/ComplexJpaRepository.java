package io.github.dengchen2020.jpa.base;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import io.github.dengchen2020.core.jdbc.Page;
import io.github.dengchen2020.core.jdbc.SimplePage;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.stream.Stream;

/**
 * 复杂CRUD的jpa操作接口
 * @author xiaochen
 * @since 2024/6/18
 */
@NullMarked
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
     * @param page 分页参数
     * @param o 排序方式
     * @return SimplePage<R>
     */
    <R> SimplePage<R> fetchPage(JPAQuery<R> query, Page page, OrderSpecifier<?>... o);

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
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体会有一级缓存，因此依然可能存在严重的内存占用，以下情况除外 </br>
     * <p>1.使用{@link EntityManager#detach(Object)}，例如：
     * <pre>
     * {@code
     * stream.forEach(entity -> {
     *     ... 处理业务逻辑
     *     entityManager.detach(entity); // 从持久上下文中分离JPA实体，意味着释放了一级缓存
     * });
     * }
     * </pre>
     * </p>
     * 2.使用自定义类型投影，{@link Projections#bean(Class, Expression[])}或{@link Projections#constructor(Class, Expression[])}，例如：
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.bean(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.constructor(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.bean(R.class,q_r)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.constructor(R.class,q_r)
     * }
     * </pre>
     * @param query JPAQuery<R>
     * @param page 分页参数
     * @param o 排序方式
     * @return Stream<R>
     */
    <R> Stream<R> fetchStream(JPAQuery<R> query, @Nullable Page page, OrderSpecifier<?>... o);

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体会有一级缓存，因此依然可能存在严重的内存占用，以下情况除外 </br>
     * <p>1.使用{@link EntityManager#detach(Object)}，例如：
     * <pre>
     * {@code
     * stream.forEach(entity -> {
     *     ... 处理业务逻辑
     *     entityManager.detach(entity); // 从持久上下文中分离JPA实体，意味着释放了一级缓存
     * });
     * }
     * </pre>
     * </p>
     * 2.使用自定义类型投影，{@link Projections#bean(Class, Expression[])}或{@link Projections#constructor(Class, Expression[])}，例如：
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.bean(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.constructor(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.bean(R.class,q_r)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.constructor(R.class,q_r)
     * }
     * </pre>
     * @param predicate 条件
     * @param page 分页参数
     * @param o 排序方式
     * @return Stream<T>
     */
    Stream<T> findStream(Predicate predicate,@Nullable Page page, OrderSpecifier<?>... o);

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体会有一级缓存，因此依然可能存在严重的内存占用，以下情况除外 </br>
     * <p>1.使用{@link EntityManager#detach(Object)}，例如：
     * <pre>
     * {@code
     * stream.forEach(entity -> {
     *     ... 处理业务逻辑
     *     entityManager.detach(entity); // 从持久上下文中分离JPA实体，意味着释放了一级缓存
     * });
     * }
     * </pre>
     * </p>
     * 2.使用自定义类型投影，{@link Projections#bean(Class, Expression[])}或{@link Projections#constructor(Class, Expression[])}，例如：
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.bean(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.constructor(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.bean(R.class,q_r)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.constructor(R.class,q_r)
     * }
     * </pre>
     * @param page 分页参数
     * @param o 排序方式
     * @return Stream<T>
     */
    Stream<T> findStream(@Nullable Page page, OrderSpecifier<?>... o);

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体会有一级缓存，因此依然可能存在严重的内存占用，以下情况除外 </br>
     * <p>1.使用{@link EntityManager#detach(Object)}，例如：
     * <pre>
     * {@code
     * stream.forEach(entity -> {
     *     ... 处理业务逻辑
     *     entityManager.detach(entity); // 从持久上下文中分离JPA实体，意味着释放了一级缓存
     * });
     * }
     * </pre>
     * </p>
     * 2.使用自定义类型投影，{@link Projections#bean(Class, Expression[])}或{@link Projections#constructor(Class, Expression[])}，例如：
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.bean(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.constructor(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.bean(R.class,q_r)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.constructor(R.class,q_r)
     * }
     * </pre>
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
    default <R> SimplePage<R> fetchPage(JPAQuery<R> query, Page page){
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
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体会有一级缓存，因此依然可能存在严重的内存占用，以下情况除外 </br>
     * <p>1.使用{@link EntityManager#detach(Object)}，例如：
     * <pre>
     * {@code
     * stream.forEach(entity -> {
     *     ... 处理业务逻辑
     *     entityManager.detach(entity); // 从持久上下文中分离JPA实体，意味着释放了一级缓存
     * });
     * }
     * </pre>
     * </p>
     * 2.使用自定义类型投影，{@link Projections#bean(Class, Expression[])}或{@link Projections#constructor(Class, Expression[])}，例如：
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.bean(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.constructor(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.bean(R.class,q_r)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.constructor(R.class,q_r)
     * }
     * </pre>
     * @param query JPAQuery<R>
     * @param page 分页参数
     * @return Stream<R>
     */
    default <R> Stream<R> fetchStream(JPAQuery<R> query,@Nullable Page page){
        return fetchStream(query, page, EMPTY_ORDER_SPECIFIER);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体会有一级缓存，因此依然可能存在严重的内存占用，以下情况除外 </br>
     * <p>1.使用{@link EntityManager#detach(Object)}，例如：
     * <pre>
     * {@code
     * stream.forEach(entity -> {
     *     ... 处理业务逻辑
     *     entityManager.detach(entity); // 从持久上下文中分离JPA实体，意味着释放了一级缓存
     * });
     * }
     * </pre>
     * </p>
     * 2.使用自定义类型投影，{@link Projections#bean(Class, Expression[])}或{@link Projections#constructor(Class, Expression[])}，例如：
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.bean(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.constructor(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.bean(R.class,q_r)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.constructor(R.class,q_r)
     * }
     * </pre>
     * @param predicate 条件
     * @param page 分页参数
     * @return Stream<T>
     */
    default Stream<T> findStream(Predicate predicate,@Nullable Page page){
        return findStream(predicate, page, EMPTY_ORDER_SPECIFIER);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体会有一级缓存，因此依然可能存在严重的内存占用，以下情况除外 </br>
     * <p>1.使用{@link EntityManager#detach(Object)}，例如：
     * <pre>
     * {@code
     * stream.forEach(entity -> {
     *     ... 处理业务逻辑
     *     entityManager.detach(entity); // 从持久上下文中分离JPA实体，意味着释放了一级缓存
     * });
     * }
     * </pre>
     * </p>
     * 2.使用自定义类型投影，{@link Projections#bean(Class, Expression[])}或{@link Projections#constructor(Class, Expression[])}，例如：
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.bean(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.constructor(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.bean(R.class,q_r)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.constructor(R.class,q_r)
     * }
     * </pre>
     * @param page 分页参数
     * @return Stream<T>
     */
    default Stream<T> findStream(@Nullable Page page){
        return findStream(page, EMPTY_ORDER_SPECIFIER);
    }

    /**
     * 返回流读取器，调用方需手动关闭以便尽快释放资源
     * @warn 注意：当前Spring中默认使用的是有状态会话，JPA实体会有一级缓存，因此依然可能存在严重的内存占用，以下情况除外 </br>
     * <p>1.使用{@link EntityManager#detach(Object)}，例如：
     * <pre>
     * {@code
     * stream.forEach(entity -> {
     *     ... 处理业务逻辑
     *     entityManager.detach(entity); // 从持久上下文中分离JPA实体，意味着释放了一级缓存
     * });
     * }
     * </pre>
     * </p>
     * 2.使用自定义类型投影，{@link Projections#bean(Class, Expression[])}或{@link Projections#constructor(Class, Expression[])}，例如：
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.bean(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 无一级缓存，内存占用低
     * Projections.constructor(R.class,q_r.id,q_r.name,q_r.otherField)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.bean(R.class,q_r)
     * }
     * </pre>
     * <pre>
     * {@code
     * // 存在一级缓存，内存占用高，因此不可直接传入JPA实体
     * Projections.constructor(R.class,q_r)
     * }
     * </pre>
     * @return Stream<T>
     */
    default Stream<T> findStream(){
        return findStream(EMPTY_ORDER_SPECIFIER);
    }

}
