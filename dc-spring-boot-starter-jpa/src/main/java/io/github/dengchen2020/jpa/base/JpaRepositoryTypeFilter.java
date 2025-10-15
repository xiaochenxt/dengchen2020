package io.github.dengchen2020.jpa.base;

import jakarta.annotation.Nonnull;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.IOException;

/**
 * 匹配Jpa存储库
 *
 * @author xiaochen
 * @since 2024/6/15
 */
public class JpaRepositoryTypeFilter implements TypeFilter {

    private static final String noRepositoryBeanClassName = NoRepositoryBean.class.getName();

    @Override
    public boolean match(@Nonnull MetadataReader metadataReader, @Nonnull MetadataReaderFactory metadataReaderFactory) throws IOException {
        if (!metadataReader.getClassMetadata().isInterface()) return false;
        String[] interfaces = metadataReader.getClassMetadata().getInterfaceNames();
        for (String interfaceName : interfaces) {
            AnnotationMetadata annotationMetadata = metadataReaderFactory.getMetadataReader(interfaceName).getAnnotationMetadata();
            if (annotationMetadata.hasAnnotation(noRepositoryBeanClassName)) return true;
        }
        return false;
    }

}
