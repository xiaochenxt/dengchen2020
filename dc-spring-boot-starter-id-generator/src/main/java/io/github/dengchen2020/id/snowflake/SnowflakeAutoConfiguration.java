package io.github.dengchen2020.id.snowflake;

import io.github.dengchen2020.id.properties.IdGeneratorBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 雪花算法自动配置
 * <p>依赖redis，需引入</p>
 * <pre>{@code
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-data-redis</artifactId>
 * </dependency>}</pre>
 * @author xiaochen
 * @since 2024/7/2
 */
@ConditionalOnBean(RedisConnectionFactory.class)
@EnableConfigurationProperties(IdGeneratorBuilder.class)
@ConditionalOnProperty(value = "dc.id.type",matchIfMissing = true,havingValue = "snowflake")
@Configuration(proxyBeanMethods = false)
public final class SnowflakeAutoConfiguration {

    @Bean
    SnowflakeSmartLifecycle snowflakeSmartLifecycle(RedisConnectionFactory redisConnectionFactory, IdGeneratorBuilder idGeneratorBuilder){
        return new SnowflakeSmartLifecycle(new StringRedisTemplate(redisConnectionFactory),idGeneratorBuilder.getSnowflake());
    }

}
