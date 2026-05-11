package io.github.dengchen2020.jpa.base;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * JPA支持
 * @author xiaochen
 * @since 2026/5/11
 */
final class JpaSupport {

    private static final ConcurrentMap<Class<?>, JpaEntityInformation<?, ?>> jpaEntityInformationCache = new ConcurrentHashMap<>(32);

    static JpaEntityInformation<?, ?> getJpaEntityInformation(Class<?> domainClass, EntityManager entityManager) {
        return jpaEntityInformationCache.computeIfAbsent(domainClass, (c) -> JpaEntityInformationSupport.getEntityInformation(c, entityManager));
    }

}
