package io.github.dengchen2020.cache;

/**
 * 缓存操作-接口
 * @author xiaochen
 * @since 2024/7/31
 */
public interface CacheHelper {

    /**
     * 移除cacheName指定key的缓存
     *
     * @param cacheName 缓存名
     * @param key       key
     */
    void evict(String cacheName, Object key);

    /**
     * 移除cacheName指定key的缓存
     *
     * @param cacheNames 缓存名
     * @param key        key
     */
     void evict(String[] cacheNames, Object key);

    /**
     * 移除cacheName的所有缓存
     *
     * @param cacheName 缓存名
     */
    void clear(String cacheName);

    /**
     * 移除cacheName的所有缓存
     *
     * @param cacheNames 缓存名
     */
    void clear(String[] cacheNames);


    /**
     * 清除所有缓存
     */
    void clearAll();

}
