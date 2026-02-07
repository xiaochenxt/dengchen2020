package io.github.dengchen2020.lock.config;

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
import org.springframework.context.aot.AbstractAotProcessor;
import org.springframework.core.env.Environment;

import java.util.concurrent.Executors;

/**
 * 锁自动配置
 * @author xiaochen
 * @since 2024/7/1
 */
@ConditionalOnClass(RedissonClient.class)
@Configuration(proxyBeanMethods = false)
public final class LockAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean(destroyMethod = "shutdown")
    RedissonClient redissonClient(Environment environment) {
        Config config = new Config();
        config.setNettyExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("redisson-netty-", 0).factory()));
        config.setExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("redisson-", 0).factory()));
        config.setUseScriptCache(true);
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setConnectionMinimumIdleSize(1);
        singleServerConfig.setConnectionPoolSize(200);
        String host = environment.getProperty("dc.lock.redisson.redis.host", environment.getProperty("spring.data.redis.host","127.0.0.1"));
        int port = environment.getProperty("dc.lock.redisson.redis.port", int.class, environment.getProperty("spring.data.redis.port", int.class, 6379));
        int database = environment.getProperty("dc.lock.redisson.redis.database", int.class, environment.getProperty("spring.data.redis.database", int.class, 0));
        config.setTcpKeepAlive(true);
        singleServerConfig.setAddress("redis://" + host + ":" + port).setDatabase(database);
        String password = environment.getProperty("dc.lock.redisson.redis.password");
        if (password == null) password = environment.getProperty("spring.data.redis.password");
        if (password != null) config.setPassword(password);
        String username = environment.getProperty("dc.lock.redisson.redis.username");
        if (username == null) username = environment.getProperty("spring.data.redis.username");
        if (username != null) config.setUsername(username);
        String applicationName = environment.getProperty("spring.application.name","spring");
        singleServerConfig.setClientName(applicationName + "-redisson-lock");
        if (environment.getProperty("dc.lock.redisson.redis.lazy-initialization", boolean.class, true)) {
            config.setLazyInitialization(true);
        }else {
            // 在spring-aot处理器执行阶段不连接redis，避免使用其他环境的redis配置连不上而报错（编译环境与运行环境不一定相同，redis可能是内网的）
            if (Boolean.parseBoolean(System.getProperty(AbstractAotProcessor.AOT_PROCESSING))) {
                config.setLazyInitialization(true);
            }
        }
        return Redisson.create(config);
    }

    @Bean
    RedissonLock redissonLock(RedissonClient redissonClient) {
        return new RedissonLock(redissonClient);
    }

    @Bean
    LockAop lockAop(RedissonClient redissonClient) {
        return new LockAop(redissonClient);
    }

}
