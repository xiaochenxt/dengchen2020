package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.PathBuilder;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 缓存支持
 * @author xiaochen
 * @since 2025/12/8
 */
class CacheSupport {

    static final ConcurrentMap<Class<?>, EntityPath<?>> entityPathMap = new ConcurrentHashMap<>();
    static final ConcurrentMap<Class<?>, PathBuilder<?>> pathBuilderMap = new ConcurrentHashMap<>();

    public static void clear(){
        entityPathMap.clear();
        pathBuilderMap.clear();
    }

}
