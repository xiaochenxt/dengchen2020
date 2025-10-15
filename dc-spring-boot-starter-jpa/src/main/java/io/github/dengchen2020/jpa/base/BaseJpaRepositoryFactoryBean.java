package io.github.dengchen2020.jpa.base;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.*;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import java.util.List;

/**
 * BaseRepository的工厂
 * @author xiaochen
 * @since 2024/1/19
 */
public class BaseJpaRepositoryFactoryBean<T extends Repository<S, ID>, S, ID> extends JpaRepositoryFactoryBean<T, S, ID> {

    private JPAQueryFactory jpaQueryFactory;

    private List<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer;

    @Autowired
    public void setJpaQueryFactory(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Autowired
    public void setRepositoryFragmentsCustomizer(@Nullable List<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer) {
        this.repositoryFragmentsCustomizer = repositoryFragmentsCustomizer;
    }

    public BaseJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    @Nonnull
    @Override
    protected RepositoryFactorySupport createRepositoryFactory(@Nonnull EntityManager em) {
        return new BaseRepositoryFactory(em, jpaQueryFactory, repositoryFragmentsCustomizer);
    }

    private static class BaseRepositoryFactory
            extends JpaRepositoryFactory {

        private final JPAQueryFactory jpaQueryFactory;

        private final List<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer;

        public BaseRepositoryFactory(EntityManager em, JPAQueryFactory jpaQueryFactory, List<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizers) {
            super(em);
            this.jpaQueryFactory = jpaQueryFactory;
            this.repositoryFragmentsCustomizer = repositoryFragmentsCustomizers;
        }

        @Nonnull
        @Override
        protected JpaRepositoryImplementation<?, ?> getTargetRepository(@Nonnull final RepositoryInformation information,@Nonnull final EntityManager entityManager) {
            JpaEntityInformation<?, ?> entityInformation = getEntityInformation(information.getDomainType());
            return getTargetRepositoryViaReflection(information,entityInformation,entityManager,jpaQueryFactory);
        }

        /**
         * 设置具体的实现类的class
         * @param metadata 存储库接口的元数据
         * @return {@link Class}
         */
        @Nonnull
        @Override
        protected Class<?> getRepositoryBaseClass(@Nonnull RepositoryMetadata metadata) {
            return BaseJpaRepositoryExecutor.class;
        }

        @Override
        protected RepositoryComposition.RepositoryFragments getRepositoryFragments(RepositoryMetadata metadata, EntityManager entityManager, EntityPathResolver resolver, CrudMethodMetadata crudMethodMetadata) {
            var fragments = super.getRepositoryFragments(metadata, entityManager, resolver, crudMethodMetadata);
            if (repositoryFragmentsCustomizer != null) {
                for (RepositoryFragmentsCustomizer customizer : repositoryFragmentsCustomizer) {
                    fragments = customizer.customize(fragments, metadata, entityManager, resolver, crudMethodMetadata);
                }
            }
            return fragments;
        }
    }
}
