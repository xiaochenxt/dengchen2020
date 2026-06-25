package io.github.dengchen2020.jpa.base;

import jakarta.persistence.EntityManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
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

    private EntityPathResolver entityPathResolver;
    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;
    private JpaQueryMethodFactory queryMethodFactory;

    @Autowired
    public void setRepositoryFragmentsCustomizer(ObjectProvider<RepositoryFragmentsCustomizer> repositoryFragmentsCustomizer) {
        this.repositoryFragmentsCustomizer = repositoryFragmentsCustomizer;
    }

    @Autowired
    public void setEntityPathResolver(ObjectProvider<EntityPathResolver> resolver) {
        this.entityPathResolver = resolver.getIfAvailable(() -> SimpleEntityPathResolver.INSTANCE);
    }

    @Autowired
    public void setQueryMethodFactory(ObjectProvider<JpaQueryMethodFactory> factory) {
        this.queryMethodFactory = factory.getIfAvailable();
    }

    public void setEscapeCharacter(char escapeCharacter) {
        this.escapeCharacter = EscapeCharacter.of(escapeCharacter);
    }

    @NonNull
    @Override
    protected RepositoryFactorySupport createRepositoryFactory(@NonNull EntityManager em) {
        var factory = new BaseRepositoryFactory(em, repositoryFragmentsCustomizer);
        factory.setEntityPathResolver(entityPathResolver);
        factory.setEscapeCharacter(escapeCharacter);
        if (queryMethodFactory != null) factory.setQueryMethodFactory(queryMethodFactory);
        return factory;
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
