package io.github.dengchen2020.lock.config;

import io.github.dengchen2020.core.utils.IPUtils;
import io.github.dengchen2020.lock.LockAop;
import io.github.dengchen2020.lock.api.RedissonLock;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.concurrent.Executors;

/**
 * 锁自动配置
 * @author xiaochen
 * @since 2024/7/1
 */
@EnableConfigurationProperties(RedisProperties.class)
@Configuration(proxyBeanMethods = false)
public class LockAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties, Environment environment) {
        Config config = new Config();
        config.setNettyExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("redisson-netty-", 0).factory()));
        config.setExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("redisson-", 0).factory()));
        config.setUseScriptCache(true);
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setConnectionMinimumIdleSize(1);
        singleServerConfig.setConnectionPoolSize(200);
        singleServerConfig.setKeepAlive(true).setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort()).setPassword(redisProperties.getPassword()).setDatabase(redisProperties.getDatabase());
        if (redisProperties.getUsername() != null) singleServerConfig.setUsername(redisProperties.getUsername());
        String applicationName = environment.getProperty("spring.application.name","spring");
        Integer port = environment.getProperty("server.port", Integer.class);
        singleServerConfig.setClientName(applicationName + "-" + (port == null ? IPUtils.getLocalAddr() : IPUtils.getLocalAddr() + ":" + port) + "-redisson");
        return Redisson.create(config);
    }

    @ConditionalOnMissingBean
    @Bean
    public RedissonLock redissonLock(RedissonClient redissonClient) {
        return new RedissonLock(redissonClient);
    }

    @Bean
    public LockAop lockAop(RedissonClient redissonClient) {
        return new LockAop(redissonClient);
    }

}
