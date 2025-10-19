package io.github.dengchen2020.core.redis.frequency;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 频控自动配置
 * @author xiaochen
 * @since 2025/5/15
 */
@ConditionalOnBean(StringRedisTemplate.class)
@Configuration(proxyBeanMethods = false)
public class FrequencyControlAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public FrequencyControlSupport frequencyControlSupport(StringRedisTemplate redisTemplate) {
        return new FrequencyControlSupport(redisTemplate);
    }

    @ConditionalOnProperty(value = "dc.frequency-control.enabled", havingValue = "true")
    @Configuration(proxyBeanMethods = false)
    static class FrequencyControlInterceptorRegistrar implements WebMvcConfigurer {

        private final FrequencyControlSupport frequencyControlSupport;

        FrequencyControlInterceptorRegistrar(FrequencyControlSupport frequencyControlSupport) {
            this.frequencyControlSupport = frequencyControlSupport;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new FrequencyControlInterceptor(frequencyControlSupport));
        }

    }

}
