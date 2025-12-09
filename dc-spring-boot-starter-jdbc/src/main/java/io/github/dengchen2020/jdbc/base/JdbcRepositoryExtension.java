package io.github.dengchen2020.jdbc.base;

import org.springframework.data.core.TypeInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.RepositoryMethodContext;
import org.springframework.data.repository.core.support.RepositoryMetadataAccess;


/**
 * 实现通用存储库片段的辅助接口（适用于基于Impl后缀的实现类的方式）
 * @author xiaochen
 * @since 2025/11/23
 */
public interface JdbcRepositoryExtension<T, ID> extends RepositoryMetadataAccess {

    default RepositoryMetadata getRepositoryMetadata(){
        return RepositoryMethodContext.getContext().getMetadata();
    }

    /**
     * 获取当前JDBC操作的实体类型
     * @return
     */
    @SuppressWarnings("unchecked")
    default Class<T> getDomainClass(){
        return (Class<T>) getRepositoryMetadata().getDomainType();
    }

    /**
     * 获取当前JDBC操作的实体类型的ID类型
     * @return
     */
    @SuppressWarnings("unchecked")
    default Class<ID> getIdClass(){
        return (Class<ID>) getRepositoryMetadata().getIdType();
    }

    /**
     * 获取当前JDBC操作的实体类型的信息
     * @return
     */
    default TypeInformation<?> getDomainTypeInformation(){
        return getRepositoryMetadata().getDomainTypeInformation();
    }

    /**
     * 获取当前JDBC操作的实体类型的ID类型的信息
     * @return
     */
    default TypeInformation<?> getIdTypeInformation(){
        return getRepositoryMetadata().getIdTypeInformation();
    }

}
