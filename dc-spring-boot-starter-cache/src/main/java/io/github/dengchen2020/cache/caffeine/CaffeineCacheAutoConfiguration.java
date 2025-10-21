package io.github.dengchen2020.cache.caffeine;

import io.github.dengchen2020.cache.properties.CacheSpecBuilder;
import io.github.dengchen2020.core.redis.RedisDependencyAutoConfiguration;
import io.github.dengchen2020.core.redis.RedisMessagePublisher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.Nullable;
import org.springframework.data.redis.connection.MessageListener;

/**
 * Caffeine缓存自动配置
 *
 * @author xiaochen
 * @since 2024/5/30
 */
@AutoConfigureAfter(RedisDependencyAutoConfiguration.class)
@EnableConfigurationProperties(CacheSpecBuilder.class)
@ConditionalOnProperty(value = "spring.cache.type", matchIfMissing = true, havingValue = "caffeine")
@Configuration(proxyBeanMethods = false)
public final class CaffeineCacheAutoConfiguration {

    @ConditionalOnBean(RedisMessagePublisher.class)
    @ConditionalOnMissingBean
    @Bean
    CaffeineCacheHelper caffeineCacheHelper(RedisMessagePublisher redisMessagePublisher){
        return new CaffeineCacheHelper(redisMessagePublisher);
    }

    @ConditionalOnMissingBean
    @Bean
    CaffeineCacheManager caffeineCacheManager(CacheSpecBuilder cacheSpecBuilder,@Nullable CaffeineCacheHelper cacheHelper) {
        return new CaffeineCacheManager(cacheSpecBuilder.getCaffeine(), cacheHelper);
    }

    /**
     * Caffeine缓存同步自动配置
     * <p>依赖redis的发布订阅，需引入</p>
     * <pre>{@code
     * <dependency>
     *     <groupId>org.springframework.boot</groupId>
     *     <artifactId>spring-boot-starter-data-redis</artifactId>
     * </dependency>}</pre>
     *
     * @author xiaochen
     * @since 2024/7/3
     */
    @ConditionalOnClass(MessageListener.class)
    @Configuration(proxyBeanMethods = false)
    static final class CaffeineCacheSyncAutoConfiguration {

        @ConditionalOnMissingBean
        @Bean
        CacheSyncMessageListener cacheSyncMessageListener(CacheManager cacheManager){
            return new CacheSyncMessageListener(cacheManager);
        }

    }

}
