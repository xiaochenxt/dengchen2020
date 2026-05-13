package io.github.dengchen2020.jpa.base;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFragmentsContributor;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;

/**
 * JDBC仓库片段贡献者
 * @author xiaochen
 * @since 2026/5/12
 */
public class QuerydslJdbcRepositoryFragmentsContributor implements JpaRepositoryFragmentsContributor {

    public static final boolean SQL_TEMPLATES_PRESENT = ClassUtils
            .isPresent("com.querydsl.sql.SQLTemplates", QuerydslJdbcRepositoryFragmentsContributor.class.getClassLoader());

    private final DataSource dataSource;

    public QuerydslJdbcRepositoryFragmentsContributor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private boolean isSupportedRepositoryInterface(RepositoryMetadata metadata) {
        return SQL_TEMPLATES_PRESENT && QuerydslJdbcRepository.class.isAssignableFrom(metadata.getRepositoryInterface());
    }

    @Override
    public RepositoryComposition.RepositoryFragments contribute(RepositoryMetadata metadata, JpaEntityInformation<?, ?> entityInformation, EntityManager entityManager, EntityPathResolver resolver) {
        if (!isSupportedRepositoryInterface(metadata)) return RepositoryComposition.RepositoryFragments.empty();
        var executor = new QuerydslJdbcRepositoryExecutor<>(entityInformation, entityManager, resolver, dataSource);
        return RepositoryComposition.RepositoryFragments
                .of(RepositoryFragment.implemented(QuerydslJdbcRepositoryExecutor.class, executor));
    }

    @Override
    public RepositoryComposition.RepositoryFragments describe(RepositoryMetadata metadata) {
        if (!isSupportedRepositoryInterface(metadata)) return RepositoryComposition.RepositoryFragments.empty();
        return RepositoryComposition.RepositoryFragments
                .of(RepositoryFragment.structural(QuerydslJdbcRepositoryExecutor .class, QuerydslJdbcRepositoryExecutor.class));
    }

}
