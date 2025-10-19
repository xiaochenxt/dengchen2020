package io.github.dengchen2020.core.redis;

import io.github.dengchen2020.core.utils.IPUtils;
import io.lettuce.core.RedisClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 引入Lettuce依赖的自动配置
 * @author xiaochen
 * @since 2025/10/18
 */
@ConditionalOnProperty(value = "spring.data.redis.client-name", matchIfMissing = true)
@ConditionalOnClass(RedisClient.class)
@Configuration(proxyBeanMethods = false)
public class LettuceDependencyAutoConfiguration {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer dcLettuceClientConfigurationBuilderCustomizer(Environment environment){
        return clientConfigurationBuilder -> {
            String applicationName = environment.getProperty("spring.application.name", "spring");
            Integer port = environment.getProperty("server.port", Integer.class);
            clientConfigurationBuilder.clientName(applicationName + "-" + (port == null ? IPUtils.getLocalAddr() : IPUtils.getLocalAddr() + ":" + port));
        };
    }

}
