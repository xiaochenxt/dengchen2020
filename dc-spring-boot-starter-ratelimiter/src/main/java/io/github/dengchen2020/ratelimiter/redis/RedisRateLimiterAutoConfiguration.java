package io.github.dengchen2020.ratelimiter.redis;

import io.github.dengchen2020.ratelimiter.properties.RateLimiterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Redis分布式限流自动配置
 * @author xiaochen
 * @since 2024/8/3
 */
@ConditionalOnBean(StringRedisTemplate.class)
@EnableConfigurationProperties(RateLimiterProperties.class)
@Configuration(proxyBeanMethods = false)
public final class RedisRateLimiterAutoConfiguration implements WebMvcConfigurer {

    private final RateLimiterProperties properties;

    private final StringRedisTemplate redisTemplate;

    RedisRateLimiterAutoConfiguration(RateLimiterProperties properties, StringRedisTemplate stringRedisTemplate) {
        this.properties = properties;
        this.redisTemplate = stringRedisTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (properties.isEnabled()) registry.addInterceptor(new RedisRateLimiterInterceptor(new RedisRateLimiter(redisTemplate), properties.getErrorMsg()));
    }

}
