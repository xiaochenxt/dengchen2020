package io.github.dengchen2020.jpa.aot;

import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import io.github.dengchen2020.aot.utils.AotUtils;
import io.github.dengchen2020.jpa.base.BaseJpaRepositoryExecutor;
import io.github.dengchen2020.jpa.base.QuerydslJdbcRepositoryExecutor;
import io.github.dengchen2020.jpa.base.QuerydslJpaRepositoryExecutor;
import org.springframework.aot.hint.*;

import java.util.List;

/**
 * JPA运行时提示注册器
 * @author xiaochen
 * @since 2025/5/23
 */
public class JpaRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = new AotUtils(hints, classLoader);
        aotUtils.registerReflection(builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS), PathBuilder.class, JPAQuery.class);
        aotUtils.registerReflection(builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS), BaseJpaRepositoryExecutor.class, QuerydslJpaRepositoryExecutor.class);
        if (aotUtils.isPresent("com.querydsl.sql.SQLTemplates")) {
            aotUtils.registerReflection(QuerydslJdbcRepositoryExecutor .class);
            aotUtils.registerPattern("keywords/*");
            aotUtils.registerReflectionIfPresent("com.querydsl.sql.SQLTemplates");
        }
        var spatialService = aotUtils.loadClass("org.hibernate.spatial.integration.SpatialService");
        if (spatialService != null) {
            hints.reflection().registerType(TypeReference.of("org.hibernate.spatial.HSMessageLogger_$logger"),
                    builder -> builder.onReachableType(TypeReference.of(spatialService))
                            .withMethod("<init>", List.of(TypeReference.of("org.jboss.logging.Logger")), ExecutableMode.INVOKE));
            aotUtils.registerReflection(builder -> builder.onReachableType(TypeReference.of(spatialService)), "org.hibernate.spatial.contributor.SpatialFunctionContributor",
                    "org.hibernate.spatial.contributor.SpatialTypeContributor","org.hibernate.spatial.criteria.internal.GeolatteSpatialCriteriaExtension",
                    "org.hibernate.spatial.criteria.internal.JTSSpatialCriteriaExtension","org.hibernate.spatial.integration.SpatialInitializer");
        }
    }

}
