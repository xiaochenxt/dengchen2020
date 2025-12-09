package io.github.dengchen2020.jpa.base;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.util.List;

import static org.springframework.data.querydsl.QuerydslUtils.QUERY_DSL_PRESENT;

/**
 * BaseRepository的工厂
 * @author xiaochen
 * @since 2024/1/19
 */
public class BaseJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JpaRepositoryFactoryBean<T, S, ID> {

    private List<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer;

    @Autowired
    public void setRepositoryFragmentsCustomizer(@jakarta.annotation.Nullable List<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer) {
        this.repositoryFragmentsCustomizer = repositoryFragmentsCustomizer;
    }

    public BaseJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Nonnull
    @Override
    protected RepositoryFactorySupport createRepositoryFactory(@Nonnull EntityManager em) {
        return new BaseRepositoryFactory(em, repositoryFragmentsCustomizer);
    }

    @NullMarked
    private static class BaseRepositoryFactory
            extends JpaRepositoryFactory {

        @Nullable
        private final List<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer;

        public BaseRepositoryFactory(EntityManager em,@Nullable List<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizers) {
            super(em);
            this.repositoryFragmentsCustomizer = repositoryFragmentsCustomizers;
        }

        /**
         * 设置具体的实现类的class
         * @param metadata 存储库接口的元数据
         * @return {@link Class}
         */
        @Override
        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return BaseJpaRepositoryExecutor.class;
        }

        @Override
        protected RepositoryComposition.RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata, EntityManager entityManager, EntityPathResolver resolver, CrudMethodMetadata crudMethodMetadata) {
            var fragments = RepositoryComposition.RepositoryFragments.just();
            var entityInformation = getEntityInformation(metadata.getDomainType());
            if (repositoryFragmentsCustomizer != null) {
                for (RepositoryFragmentsCustomizer customizer : repositoryFragmentsCustomizer) {
                    fragments = customizer.customize(fragments, metadata, entityInformation, entityManager, resolver, crudMethodMetadata);
                }
            }
            boolean isQueryDslRepository = QUERY_DSL_PRESENT
                    && (QuerydslJpaRepository.class.isAssignableFrom(metadata.getRepositoryInterface()) || ComplexJpaRepository.class.isAssignableFrom(metadata.getRepositoryInterface()));
            if (isQueryDslRepository) {
                var querydslRepositoryExecutor = new QuerydslJpaRepositoryExecutor<>(entityInformation, entityManager, resolver);
                fragments = fragments.append(RepositoryComposition.RepositoryFragments.just(querydslRepositoryExecutor));
            }
            return fragments.append(super.getRepositoryFragments(metadata, entityManager, resolver, crudMethodMetadata));
        }
    }
}
