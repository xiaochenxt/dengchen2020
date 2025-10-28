package io.github.dengchen2020.lock.config;

import io.github.dengchen2020.lock.LockAop;
import io.github.dengchen2020.lock.api.RedissonLock;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.DisposableBean;
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
public final class LockAutoConfiguration implements DisposableBean {

    private final RedissonClient redissonClient;

    LockAutoConfiguration(Environment environment) {
        Config config = new Config();
        config.setNettyExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("redisson-netty-", 0).factory()));
        config.setExecutor(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("redisson-", 0).factory()));
        config.setUseScriptCache(true);
        /*
          基于redis多节点的分布式锁RedLock已被弃用，被认为是有争议的，因此这里使用单节点redis保证锁的正确可用，
          如果担心单节点故障导致全局不可用（虽然几率低，但是不能说不会），那只能使用其他方案了，如引入zookeeper、使用数据库锁等
         */
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig.setConnectionMinimumIdleSize(1);
        singleServerConfig.setConnectionPoolSize(200);
        String host = environment.getProperty("dc.lock.redisson.redis.host", environment.getProperty("spring.data.redis.host","127.0.0.1"));
        int port = environment.getProperty("dc.lock.redisson.redis.port", int.class, environment.getProperty("spring.data.redis.port", int.class, 6379));
        int database = environment.getProperty("dc.lock.redisson.redis.database", int.class, environment.getProperty("spring.data.redis.database", int.class, 0));
        singleServerConfig.setKeepAlive(true).setAddress("redis://" + host + ":" + port).setDatabase(database);
        String password = environment.getProperty("dc.lock.redisson.redis.password");
        if (password == null) password = environment.getProperty("spring.data.redis.password");
        if (password != null) singleServerConfig.setPassword(password);
        String username = environment.getProperty("dc.lock.redisson.redis.username");
        if (username == null) username = environment.getProperty("spring.data.redis.username");
        if (username != null) singleServerConfig.setUsername(username);
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
        // 不注入redissonClient，因为需求不是操作redisson的完整功能只是需要它的分布式锁实现
        redissonClient = Redisson.create(config);
    }

    @Override
    public void destroy() {
        redissonClient.shutdown();
    }

    @ConditionalOnMissingBean
    @Bean
    RedissonLock redissonLock() {
        return new RedissonLock(redissonClient);
    }

    @Bean
    LockAop lockAop() {
        return new LockAop(redissonClient);
    }

}
