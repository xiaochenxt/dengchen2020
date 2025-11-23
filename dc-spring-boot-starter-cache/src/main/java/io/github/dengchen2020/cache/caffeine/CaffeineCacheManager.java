package io.github.dengchen2020.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.github.dengchen2020.cache.properties.CacheSpecBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.transaction.AbstractTransactionSupportingCacheManager;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * caffeine缓存管理器
 *
 * @author xiaochen
 * @since 2024/5/28
 */
@NullMarked
public class CaffeineCacheManager extends AbstractTransactionSupportingCacheManager {

    private static final Logger log = LoggerFactory.getLogger(CaffeineCacheManager.class);

    public CaffeineCacheManager(CacheSpecBuilder.Caffeine builder,@Nullable CaffeineCacheHelper cacheHelper) {
        this.builder = builder;
        this.cacheHelper = cacheHelper;
    }

   // Executor executor = new VirtualThreadTaskExecutor("caffeine-async-");

    Scheduler scheduler = Scheduler.forScheduledExecutorService(Executors.newScheduledThreadPool(1, Thread.ofVirtual().name("caffeine-cleaner",0).factory()));

    private final CacheSpecBuilder.Caffeine builder;

    @Nullable
    private final CaffeineCacheHelper cacheHelper;

    public Cache buildCache(String name, CacheSpecBuilder.Caffeine.CacheSpec cacheSpec) {
        if (cacheSpec.getExpireAfterAccess() == null) cacheSpec.setExpireAfterAccess(builder.isExpireAfterAccess());
        if (cacheSpec.getExpireTime() == null || cacheSpec.getExpireTime().compareTo(Duration.ofSeconds(1)) < 0) cacheSpec.setExpireTime(builder.getExpireTime());
        if (cacheSpec.getMax() == null || cacheSpec.getMax() < 1) cacheSpec.setMax(builder.getMax());
        if (cacheSpec.getSoftValues() == null) cacheSpec.setSoftValues(builder.isSoftValues());
        if (log.isDebugEnabled()) {
            log.debug("缓存名：{}，策略：{}，最大容量：{}", name, cacheSpec.getExpireAfterAccess() ? "读取后" + cacheSpec.getExpireTime().getSeconds() + "秒后过期" : "写入后" + cacheSpec.getExpireTime().getSeconds() + "秒后过期", cacheSpec.getMax());
        }
        final Caffeine<Object, Object> caffeineBuilder
                = Caffeine.newBuilder();
        if (cacheSpec.getExpireAfterAccess()) {
            caffeineBuilder.expireAfterAccess(cacheSpec.getExpireTime());
        } else {
            caffeineBuilder.expireAfterWrite(cacheSpec.getExpireTime());
        }
        Caffeine<Object, Object> caffeine = caffeineBuilder
                .scheduler(scheduler)
               // .executor(executor) // 使用同步模式，因此不需要异步执行器
                .maximumSize(cacheSpec.getMax());
        if (cacheSpec.getSoftValues()) caffeine.softValues();
        com.github.benmanes.caffeine.cache.Cache<Object, Object> cache = caffeine.build();
        if(cacheHelper == null) return new org.springframework.cache.caffeine.CaffeineCache(name, cache);
        return new CaffeineCache(name, cache, cacheHelper);
    }

    @Override
    protected Collection<? extends Cache> loadCaches() {
        if (builder.getSpecs().isEmpty()) return Collections.emptyList();
        List<Cache> list = new ArrayList<>();
        for (Map.Entry<String, CacheSpecBuilder.Caffeine.CacheSpec> entry : builder.getSpecs().entrySet()) {
            Cache cache = buildCache(entry.getKey(), entry.getValue());
            list.add(cache);
        }
        return list;
    }

    @Override
    protected Cache getMissingCache(String name) {
        return buildCache(name, new CacheSpecBuilder.Caffeine.CacheSpec());
    }

    @Override
    public boolean isTransactionAware() {
        return builder.isTransactionAware();
    }
}
