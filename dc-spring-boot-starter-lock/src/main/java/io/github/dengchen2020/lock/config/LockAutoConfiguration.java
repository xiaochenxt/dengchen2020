package io.github.dengchen2020.lock.config;

import io.github.dengchen2020.core.utils.IPUtils;
import io.github.dengchen2020.lock.LockAop;
import io.github.dengchen2020.lock.api.RedissonLock;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.concurrent.Executors;

/**
 * 锁自动配置
 * @author xiaochen
 * @since 2024/7/1
 */
@ConditionalOnClass(RedissonClient.class)
@Configuration(proxyBeanMethods = false)
public class LockAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(Environment environment) {
        Config config = new Config();
        config.setNettyExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("redisson-netty-", 0).factory()));
        config.setExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("redisson-", 0).factory()));
        config.setUseScriptCache(true);
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setConnectionMinimumIdleSize(1);
        singleServerConfig.setConnectionPoolSize(200);
        String host = environment.getProperty("spring.data.redis.host","127.0.0.1");
        int port = environment.getProperty("spring.data.redis.port", int.class, 6379);
        String password = environment.getProperty("spring.data.redis.password");
        int database = environment.getProperty("spring.data.redis.database", int.class, 0);
        singleServerConfig.setKeepAlive(true).setAddress("redis://" + host + ":" + port).setDatabase(database);
        if (password != null) singleServerConfig.setPassword(password);
        String username = environment.getProperty("spring.data.redis.username");
        if (username != null) singleServerConfig.setUsername(username);
        String applicationName = environment.getProperty("spring.application.name","spring");
        singleServerConfig.setClientName(applicationName + "-" + (IPUtils.getLocalAddr() + ":" + port) + "-redisson");
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
