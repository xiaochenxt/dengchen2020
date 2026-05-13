package io.github.dengchen2020.jdbc.base;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFragmentsContributor;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryComposition;
import org.springframework.data.repository.core.support.RepositoryFragment;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * JDBC仓库片段贡献者
 * @author xiaochen
 * @since 2026/5/12
 */
public class BaseJdbcRepositoryFragmentsContributor implements JpaRepositoryFragmentsContributor {

    private final DataSource dataSource;

    public BaseJdbcRepositoryFragmentsContributor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private boolean isSupportedRepositoryInterface(RepositoryMetadata metadata) {
        return BaseJdbcRepository.class.isAssignableFrom(metadata.getRepositoryInterface());
    }

    @Override
    public RepositoryComposition.RepositoryFragments describe(RepositoryMetadata metadata) {
        if (!isSupportedRepositoryInterface(metadata)) return RepositoryComposition.RepositoryFragments.empty();
        return RepositoryComposition.RepositoryFragments
                .of(RepositoryFragment.structural(BaseJdbcRepositoryExecutor .class, BaseJdbcRepositoryExecutor .class));
    }

    @Override
    public RepositoryComposition.RepositoryFragments contribute(RepositoryMetadata metadata, JpaEntityInformation<?, ?> entityInformation, EntityManager entityManager, EntityPathResolver resolver) {
        if (!isSupportedRepositoryInterface(metadata)) return RepositoryComposition.RepositoryFragments.empty();
        try {
            var executor = new BaseJdbcRepositoryExecutor<>(entityInformation, entityManager, resolver, dataSource);
            return RepositoryComposition.RepositoryFragments
                    .of(RepositoryFragment.implemented(BaseJdbcRepositoryExecutor .class, executor));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
