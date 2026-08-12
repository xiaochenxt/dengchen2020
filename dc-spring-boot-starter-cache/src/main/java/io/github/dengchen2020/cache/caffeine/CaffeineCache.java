package io.github.dengchen2020.cache.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import org.jspecify.annotations.NullMarked;

/**
 * caffeine缓存
 * @author xiaochen
 * @since 2024/5/29
 */
@NullMarked
public class CaffeineCache extends org.springframework.cache.caffeine.CaffeineCache {

    private final CaffeineCacheHelper cacheHelper;

    public CaffeineCache(String name, Cache<Object, Object> cache, CaffeineCacheHelper cacheHelper) {
        super(name, cache);
        this.cacheHelper = cacheHelper;
    }

    public CaffeineCache(String name, AsyncCache<Object, Object> cache, CaffeineCacheHelper cacheHelper) {
        super(name, cache, true);
        this.cacheHelper = cacheHelper;
    }

    @Override
    public void evict(Object key) {
        super.evict(key);
        cacheHelper.evict(getName(), key);
    }

    @Override
    public void clear() {
        super.clear();
        cacheHelper.clear(getName());
    }

}
