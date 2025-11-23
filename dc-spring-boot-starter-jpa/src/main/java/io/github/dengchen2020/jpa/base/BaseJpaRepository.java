package io.github.dengchen2020.jpa.base;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.querydsl.ListQuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * JPA操作通用接口
 *
 * @author xiaochen
 * @since 2019/2/28 11:03
 */
@NullMarked
@NoRepositoryBean
public interface BaseJpaRepository<T, ID> extends ComplexJpaRepository<T> ,
        QueryDslJpaRepository<T>, EntityManagerRepository<T, ID>,
        QueryJpaRepository<T, ID>,
        CrudJpaRepository<T, ID>, ListQuerydslPredicateExecutor<T> {

}
