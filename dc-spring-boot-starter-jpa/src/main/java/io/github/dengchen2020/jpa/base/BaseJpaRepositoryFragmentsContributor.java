package io.github.dengchen2020.jpa.base;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFragmentsContributor;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;

/**
 * JPA仓库片段贡献者
 * @author xiaochen
 * @since 2026/5/12
 */
public final class BaseJpaRepositoryFragmentsContributor implements JpaRepositoryFragmentsContributor {

    private BaseJpaRepositoryFragmentsContributor() {
    }

    public static final JpaRepositoryFragmentsContributor INSTANCE = new BaseJpaRepositoryFragmentsContributor();

    private boolean isSupportedRepositoryInterface(RepositoryMetadata metadata) {
        return QueryJpaRepository.class.isAssignableFrom(metadata.getRepositoryInterface()) || EntityManagerRepository.class.isAssignableFrom(metadata.getRepositoryInterface());
    }

    @Override
    public RepositoryComposition.RepositoryFragments contribute(RepositoryMetadata metadata, JpaEntityInformation<?, ?> entityInformation, EntityManager entityManager, EntityPathResolver resolver) {
        if (!isSupportedRepositoryInterface(metadata)) return RepositoryComposition.RepositoryFragments.empty();
        var executor = new BaseJpaRepositoryExecutor<>(entityInformation, entityManager);
        return RepositoryComposition.RepositoryFragments
                .of(RepositoryFragment.implemented(BaseJpaRepositoryExecutor.class, executor));
    }

    @Override
    public RepositoryComposition.RepositoryFragments describe(RepositoryMetadata metadata) {
        if (!isSupportedRepositoryInterface(metadata)) return RepositoryComposition.RepositoryFragments.empty();
        return RepositoryComposition.RepositoryFragments
                .of(RepositoryFragment.structural(BaseJpaRepositoryExecutor.class, BaseJpaRepositoryExecutor.class));
    }

}
