package io.github.dengchen2020.cache.caffeine;

import io.github.dengchen2020.cache.CacheHelper;
import io.github.dengchen2020.core.redis.RedisMessagePublisher;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.interceptor.SimpleKey;

/**
 * Caffeine缓存操作实现
 * @author xiaochen
 * @since 2024/7/31
 */
public class CaffeineCacheHelper implements CacheHelper {

    private static final Logger log = LoggerFactory.getLogger(CaffeineCacheHelper.class);

    private final RedisMessagePublisher redisMessagePublisher;

    public CaffeineCacheHelper(RedisMessagePublisher redisMessagePublisher) {
        this.redisMessagePublisher = redisMessagePublisher;
    }

    /**
     * 缓存同步
     *
     * @param param 同步参数
     */
    private void sync(CacheSyncParam param) {
        redisMessagePublisher.publish(CacheSyncMessageListener.CACHE_SYNC, param);
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
        Class<?> keyClass = key.getClass();
        CacheSyncParam cacheSyncParam;
        if (keyClass == String.class || keyClass.isPrimitive() || keyClass.getSuperclass() == Number.class) {
            cacheSyncParam = new CacheSyncParam(cacheNames, key);
        } else {
            cacheSyncParam = new CacheSyncParam(cacheNames);
            if (keyClass != SimpleKey.class) {
                log.warn("缓存名：{}，缓存key{}为对象类型，无法解析，清空名下所有缓存", cacheNames, key);
            }else {
                log.warn("缓存名：{}，未指定缓存key，清空名下所有缓存，如确定清空名下所有缓存，请配置@CacheEvict(value = \"{}\", allEntries = true)", cacheNames, String.join(",", cacheNames));
            }
        }
        sync(cacheSyncParam);
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
        sync(new CacheSyncParam(cacheNames));
    }

    /**
     * 清除所有缓存
     */
    @Override
    public void clearAll() {
        sync(new CacheSyncParam());
    }
}
