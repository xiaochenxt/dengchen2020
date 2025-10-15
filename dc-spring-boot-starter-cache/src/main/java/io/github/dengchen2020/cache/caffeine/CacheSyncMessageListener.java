package io.github.dengchen2020.cache.caffeine;

import io.github.dengchen2020.core.redis.annotation.RedisMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * 缓存同步消息订阅
 *
 * @author xiaochen
 * @since 2022/12/15
 */
public class CacheSyncMessageListener extends MessageListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(CacheSyncMessageListener.class);

    public static final String CACHE_SYNC = "dc:cache:sync";

    private final CacheManager cacheManager;

    public CacheSyncMessageListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @RedisMessageListener(CACHE_SYNC)
    public void handleMessage(CacheSyncParam cacheSync) {
        if(cacheSync.getCacheName() == null) {
            for (String cacheName : cacheManager.getCacheNames()) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) cache.invalidate();
            }
            if (log.isDebugEnabled()) log.debug("所有缓存被清除");
        }else {
            for (String cacheName : cacheSync.getCacheName()) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache == null) continue;
                switch (cacheSync.getType()) {
                    case 1 -> {
                        if (cache.evictIfPresent(cacheSync.getKey())) {
                            if (log.isDebugEnabled()) log.debug("缓存名：{}，key：{}被同步清除", cacheSync.getCacheName(), cacheSync.getKey());
                        }
                    }
                    case 2 -> {
                        if (cache.invalidate()) {
                            if (log.isDebugEnabled()) log.debug("缓存名：{}，所有缓存被清除", cacheName);
                        }
                    }
                }
            }
        }
    }

}
