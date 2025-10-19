package io.github.dengchen2020.cache.redis;

import io.github.dengchen2020.cache.DefaultCacheHelper;
import io.github.dengchen2020.cache.properties.CacheSpecBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存自动配置
 *
 * @author xiaochen
 * @since 2024/5/30
 */
@ConditionalOnBean(RedisConnectionFactory.class)
@EnableConfigurationProperties(CacheSpecBuilder.class)
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
@Configuration(proxyBeanMethods = false)
public class RedisCacheAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public RedisCacheManager redisCacheManager(CacheSpecBuilder cacheSpecBuilder, RedisConnectionFactory redisConnectionFactory, GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer) {
        String prefixCacheName = "dc:cache:";
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        CacheSpecBuilder.Redis builder = cacheSpecBuilder.getRedis();
        builder.getSpecs().forEach((s, cacheSpec) -> {
            if (cacheSpec.getExpireTime() == null || cacheSpec.getExpireTime().compareTo(Duration.ofSeconds(1)) < 0) cacheSpec.setExpireTime(builder.getExpireTime());
            RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().prefixCacheNameWith(prefixCacheName).entryTtl(cacheSpec.getExpireTime())
                    .serializeValuesWith(RedisSerializationContext.fromSerializer(genericJackson2JsonRedisSerializer).getValueSerializationPair());
            cacheConfigurations.put(s, redisCacheConfiguration);
        });
        RedisCacheConfiguration defaultCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig().prefixCacheNameWith(prefixCacheName).entryTtl(builder.getExpireTime())
                .serializeValuesWith(RedisSerializationContext.fromSerializer(genericJackson2JsonRedisSerializer).getValueSerializationPair());
        RedisCacheManager.RedisCacheManagerBuilder redisCacheManagerBuilder = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfiguration).withInitialCacheConfigurations(cacheConfigurations);
        if (builder.isTransactionAware()) redisCacheManagerBuilder.transactionAware();
        return redisCacheManagerBuilder.build();
    }

    @ConditionalOnMissingBean
    @Bean
    public DefaultCacheHelper defaultCacheHelper(RedisCacheManager redisCacheManager){
        return new DefaultCacheHelper(redisCacheManager);
    }

}
