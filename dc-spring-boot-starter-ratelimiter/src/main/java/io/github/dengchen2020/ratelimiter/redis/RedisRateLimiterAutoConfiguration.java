package io.github.dengchen2020.ratelimiter.redis;

import io.github.dengchen2020.ratelimiter.RateLimiterInterceptor;
import io.github.dengchen2020.ratelimiter.properties.RateLimiterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * Redis分布式限流自动配置
 * @author xiaochen
 * @since 2024/8/3
 */
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnProperty(value = "dc.ratelimiter.type", havingValue = "redis")
@EnableConfigurationProperties(RateLimiterProperties.class)
@Configuration(proxyBeanMethods = false)
public class RedisRateLimiterAutoConfiguration implements WebMvcConfigurer {

    private final RateLimiterProperties properties;

    private final StringRedisTemplate redisTemplate;

    RedisRateLimiterAutoConfiguration(RateLimiterProperties properties, StringRedisTemplate stringRedisTemplate) {
        this.properties = properties;
        this.redisTemplate = stringRedisTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (properties.isEnabled()) registry.addInterceptor(new RateLimiterInterceptor(new RedisRateLimiter(Duration.ofSeconds(1), redisTemplate), new RedisRateLimiter(Duration.ofMinutes(1), redisTemplate), properties.getErrorMsg()));
    }

}
