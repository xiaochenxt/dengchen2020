package io.github.dengchen2020.jpa.querydsl;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 提供域类与{@link EntityPath}的映射缓存支持
 * @author xiaochen
 * @since 2025/12/8
 */
public final class EntityPathCacheSupport {

    static final ConcurrentMap<Class<?>, EntityPath<?>> entityPathMap = new ConcurrentHashMap<>();
    static final ConcurrentMap<Class<?>, PathBuilder<?>> pathBuilderMap = new ConcurrentHashMap<>();

    public static EntityPath<?> getEntityPath(Class<?> domainClass) {
        return entityPathMap.computeIfAbsent(domainClass, SimpleEntityPathResolver.INSTANCE::createPath);
    }

    public static PathBuilder<?> getBuilder(Class<?> domainClass) {
        return pathBuilderMap.computeIfAbsent(domainClass, (k) -> {
            var entityPath = getEntityPath(k);
            return new PathBuilder<>(entityPath.getType(), entityPath.getMetadata());
        });
    }

    public static void clear(){
        entityPathMap.clear();
        pathBuilderMap.clear();
    }

}
