package io.github.dengchen2020.jpa.base;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;

/**
 * BaseRepository的工厂
 * @author xiaochen
 * @since 2024/1/19
 */
public class BaseJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JpaRepositoryFactoryBean<T, S, ID> {

    public BaseJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    private ObjectProvider<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer;

    @Autowired
    public void setRepositoryFragmentsCustomizer(ObjectProvider<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer) {
        this.repositoryFragmentsCustomizer = repositoryFragmentsCustomizer;
    }

    @Nonnull
    @Override
    protected RepositoryFactorySupport createRepositoryFactory(@Nonnull EntityManager em) {
        return new BaseRepositoryFactory(em, repositoryFragmentsCustomizer);
    }

    @NullMarked
    private static class BaseRepositoryFactory
            extends JpaRepositoryFactory {

        private final ObjectProvider<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer;


        public BaseRepositoryFactory(EntityManager em, ObjectProvider<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizers) {
            super(em);
            this.repositoryFragmentsCustomizer = repositoryFragmentsCustomizers;
        }

        @Override
        protected RepositoryComposition.RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata, EntityManager entityManager, EntityPathResolver resolver, CrudMethodMetadata crudMethodMetadata) {
            var entityInformation = getEntityInformation(metadata.getDomainType());
            var fragments = RepositoryComposition.RepositoryFragments.empty();
            if (QueryJpaRepository.class.isAssignableFrom(metadata.getRepositoryInterface()) || EntityManagerRepository.class.isAssignableFrom(metadata.getRepositoryInterface())) {
                fragments = fragments.append(RepositoryComposition.RepositoryFragments.just(new BaseJpaRepositoryExecutor<>(entityInformation, entityManager)));
            }
            boolean isQueryDslRepository = QUERY_DSL_PRESENT
                    && (QuerydslJpaRepository.class.isAssignableFrom(metadata.getRepositoryInterface()));
            if (isQueryDslRepository) {
                var querydslRepositoryExecutor = new QuerydslJpaRepositoryExecutor<>(entityInformation, entityManager, resolver);
                fragments = fragments.append(RepositoryComposition.RepositoryFragments.just(querydslRepositoryExecutor));
            }
            for (var customizer : repositoryFragmentsCustomizer) {
                var fragment = customizer.customize(metadata, entityInformation, entityManager, resolver);
                if (fragment != null)
                    fragments = fragments.append(RepositoryComposition.RepositoryFragments.just(fragment));
            }
            return fragments.append(super.getRepositoryFragments(metadata, entityManager, resolver, crudMethodMetadata));
        }
    }
}
