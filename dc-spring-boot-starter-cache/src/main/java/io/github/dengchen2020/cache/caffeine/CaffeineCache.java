package io.github.dengchen2020.cache.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.Nonnull;

/**
 * caffeine缓存
 * @author xiaochen
 * @since 2024/5/29
 */
public class CaffeineCache extends org.springframework.cache.caffeine.CaffeineCache {

    private final CaffeineCacheHelper cacheHelper;

    public CaffeineCache(String name, AsyncCache<Object, Object> cache, boolean allowNullValues, CaffeineCacheHelper cacheHelper) {
        super(name, cache, allowNullValues);
        this.cacheHelper = cacheHelper;
    }

    public CaffeineCache(String name, Cache<Object, Object> cache, boolean allowNullValues, CaffeineCacheHelper cacheHelper) {
        super(name, cache, allowNullValues);
        this.cacheHelper = cacheHelper;
    }

    @Override
    public void evict(@Nonnull Object key) {
        super.evict(key);
        cacheHelper.evict(getName(), key);
    }

    @Override
    public void clear() {
        super.clear();
        cacheHelper.clear(getName());
    }

}
