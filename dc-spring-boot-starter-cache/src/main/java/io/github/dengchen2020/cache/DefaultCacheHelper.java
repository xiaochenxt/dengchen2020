package io.github.dengchen2020.cache;

import jakarta.annotation.Nonnull;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

/**
 * 默认的缓存操作实现
 *
 * @author xiaochen
 * @since 2024/5/29
 */
public class DefaultCacheHelper implements CacheHelper {

    private final CacheManager cacheManager;

    public DefaultCacheHelper(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 读取cacheName指定key的缓存
     *
     * @param cacheName 缓存名
     * @param key key
     * @return 缓存值
     */
    public Object get(@Nonnull String cacheName,@Nonnull Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
           Cache.ValueWrapper valueWrapper = cache.get(key);
           if (valueWrapper != null) return valueWrapper.get();
        }
        return null;
    }

    /**
     * 移除cacheName指定key的缓存
     *
     * @param cacheName 缓存名
     * @param key       key
     */
    @Override
    public void evict(@Nonnull String cacheName,@Nonnull Object key) {
        evict(new String[]{cacheName}, key);
    }

    /**
     * 移除cacheName指定key的缓存
     *
     * @param cacheNames 缓存名
     * @param key        key
     */
    @Override
    public void evict(@Nonnull String[] cacheNames,@Nonnull Object key) {
        for (String s : cacheNames) {
            Cache cache = cacheManager.getCache(s);
            if (cache != null) cache.evict(key);
        }
    }

    /**
     * 使用新值覆盖cacheName指定key的缓存
     *
     * @param cacheName 缓存名
     * @param key       key
     * @param value 新值
     */
    public void put(@Nonnull String cacheName,@Nonnull Object key,@Nonnull Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) cache.put(key, value);
    }

    /**
     * 移除cacheName的所有缓存
     *
     * @param cacheName 缓存名
     */
    @Override
    public void clear(@Nonnull String cacheName) {
        clear(new String[]{cacheName});
    }

    /**
     * 移除cacheName的所有缓存
     *
     * @param cacheNames 缓存名
     */
    @Override
    public void clear(@Nonnull String[] cacheNames) {
        for (String s : cacheNames) {
            Cache cache = cacheManager.getCache(s);
            if (cache != null) cache.clear();
        }
    }

    /**
     * 清除所有缓存
     */
    @Override
    public void clearAll() {
        for (String s : cacheManager.getCacheNames()) {
            Cache cache = cacheManager.getCache(s);
            if (cache != null) cache.clear();
        }
    }

}
