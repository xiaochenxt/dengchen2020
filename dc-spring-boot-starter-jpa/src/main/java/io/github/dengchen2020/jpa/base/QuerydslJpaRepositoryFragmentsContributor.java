package io.github.dengchen2020.jpa.base;

import com.querydsl.jpa.EclipseLinkTemplates;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFragmentsContributor;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;

/**
 * querydsl-JPA仓库片段贡献者
 * @author xiaochen
 * @since 2026/5/12
 */
public final class QuerydslJpaRepositoryFragmentsContributor implements JpaRepositoryFragmentsContributor {

    private QuerydslJpaRepositoryFragmentsContributor() {
    }

    public static final JpaRepositoryFragmentsContributor INSTANCE = new QuerydslJpaRepositoryFragmentsContributor();

    private boolean isSupportedRepositoryInterface(RepositoryMetadata metadata) {
        return QUERY_DSL_PRESENT
                && (QuerydslJpaRepository.class.isAssignableFrom(metadata.getRepositoryInterface()) || ComplexJpaRepository.class.isAssignableFrom(metadata.getRepositoryInterface()));
    }

    @Override
    public RepositoryComposition.RepositoryFragments describe(RepositoryMetadata metadata) {
        if (!isSupportedRepositoryInterface(metadata)) return RepositoryComposition.RepositoryFragments.empty();
        return RepositoryComposition.RepositoryFragments
                .of(RepositoryFragment.structural(QuerydslJpaRepository.class, QuerydslJpaRepositoryExecutor.class));
    }

    @Override
    public RepositoryComposition.RepositoryFragments contribute(RepositoryMetadata metadata, JpaEntityInformation<?, ?> entityInformation, EntityManager entityManager, EntityPathResolver resolver) {
        if (!isSupportedRepositoryInterface(metadata)) return RepositoryComposition.RepositoryFragments.empty();
        var provider = PersistenceProvider.fromEntityManager(entityManager);
        var templates = switch (provider) {
            case ECLIPSELINK -> EclipseLinkTemplates.DEFAULT;
            case HIBERNATE -> HQLTemplates.DEFAULT;
            default -> JPQLTemplates.DEFAULT;
        };
        var jpaQueryFactory = new JPAQueryFactory(templates, entityManager);
        var executor = new QuerydslJpaRepositoryExecutor<>(entityInformation, entityManager, resolver, jpaQueryFactory);
        return RepositoryComposition.RepositoryFragments
                .of(RepositoryFragment.implemented(QuerydslJpaRepositoryExecutor.class, executor));
    }

}
