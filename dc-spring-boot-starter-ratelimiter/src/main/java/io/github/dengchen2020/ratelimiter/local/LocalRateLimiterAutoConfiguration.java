package io.github.dengchen2020.ratelimiter.local;

import io.github.dengchen2020.ratelimiter.RateLimiterInterceptor;
import io.github.dengchen2020.ratelimiter.properties.RateLimiterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * 单机限流自动配置
 *
 * @author xiaochen
 * @since 2024/7/1
 */
@ConditionalOnProperty(value = "dc.ratelimiter.type", matchIfMissing = true, havingValue = "local")
@EnableConfigurationProperties(RateLimiterProperties.class)
@Configuration(proxyBeanMethods = false)
public final class LocalRateLimiterAutoConfiguration implements WebMvcConfigurer {

    private final RateLimiterProperties properties;

    LocalRateLimiterAutoConfiguration(RateLimiterProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (properties.isEnabled()) registry.addInterceptor(new RateLimiterInterceptor(new LocalRateLimiter(Duration.ofSeconds(1)), new LocalRateLimiter(Duration.ofMinutes(1)), properties.getErrorMsg()));
    }

}
