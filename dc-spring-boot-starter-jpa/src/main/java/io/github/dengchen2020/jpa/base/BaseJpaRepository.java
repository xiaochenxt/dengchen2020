package io.github.dengchen2020.jpa.base;

import org.jspecify.annotations.NonNull;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * JPA操作通用接口
 *
 * @author xiaochen
 * @since 2019/2/28 11:03
 */
@NoRepositoryBean
public interface BaseJpaRepository<T, ID> extends CrudJpaRepository<@NonNull T,@NonNull ID>, ComplexJpaRepository<@NonNull T> ,
        QueryJpaRepository<@NonNull T,@NonNull ID>, QueryDslJpaRepository<@NonNull T> {

}
