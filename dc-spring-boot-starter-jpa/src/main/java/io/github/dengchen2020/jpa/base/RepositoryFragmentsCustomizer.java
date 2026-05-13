package io.github.dengchen2020.jpa.base;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;

/**
 * 自定义存储库片段注册
 * <p>
 * 使用示例：
 * <pre>
 * {@code
 * @RequiredArgsConstructor
 * @Component // 需注入到Spring容器中
 * public class DcRepositoryFragmentsCustomizer implements RepositoryFragmentsCustomizer {
 *
 *     private final SpringBean springBean;
 *
 *     @Override
 *     public Object customize(RepositoryMetadata metadata, JpaEntityInformation<?, ?> entityInformation, EntityManager entityManager, EntityPathResolver resolver, CrudMethodMetadata crudMethodMetadata) {
 *         // 提供一个存储库片段实现类示例，存储库片段实现类名称不要使用Impl结尾（spring默认会将Repository接口名+Impl的实现类注入到Spring容器中，不需要），最好使用Executor结尾
 *         var fragment = new CommonRepositoryExecutor<>(metadata, entityManager, springBean));
 *         return fragment;
 *     }
 * }
 * }
 * </pre>
 * </p>
 * @author xiaochen
 * @since 2025/9/30
 */
public interface RepositoryFragmentsCustomizer {

    Object customize(RepositoryMetadata metadata, JpaEntityInformation<?, ?> entityInformation, EntityManager entityManager, EntityPathResolver resolver);

}
