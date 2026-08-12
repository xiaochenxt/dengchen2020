分布式锁，支持编程方式和 `@Lock` 注解方式，基于 Redisson 实现。

应用启动时将自动创建 RedissonClient，依赖 redis，需引入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

注解方式使用：

```java
@Lock(value = "#id", name = "order", waitTime = 3, lockTime = 10, errorMsg = "系统繁忙，请稍后再试")
public void processOrder(Long id) {
    // 业务逻辑
}
```

`@Lock` 参数说明：

```java
String value() default "";       // 锁的key，支持 SpEL 表达式（如 #p0、#id）
String name() default "";        // 锁定的资源名称
long waitTime() default -1;      // 等待获取锁的时长（秒）
long lockTime() default -1;      // 持有锁的时长，-1 表示无限续期直到执行完成
TimeUnit timeUnit() default SECONDS; // 时间单位
String errorMsg() default "请求人数过多，请稍后再试"; // 异常提示
```

编程方式使用：

```java
@Resource
private DLock dLock;

// 尝试获取锁，获取失败抛异常
dLock.tryLockAndRun("order:" + id, () -> {
    // 业务逻辑
});

// 带等待时间
dLock.tryLockAndRun("order:" + id, 3, TimeUnit.SECONDS, () -> {
    // 业务逻辑
});

// 阻塞等待直到获取锁
dLock.lockAndRun("order:" + id, () -> {
    // 业务逻辑
});

// 带返回值的 Callable 方式
String result = dLock.tryLockAndRun("order:" + id, () -> {
    return callRemoteService();
});

// 自定义获取锁失败时的处理
RedissonLock redissonLock = (RedissonLock) dLock;
redissonLock.tryLockAndRun("order:" + id, () -> {
    // 成功执行
}, () -> {
    // 获取锁失败处理
});
```

properties 配置：

```properties
# Redisson 连接配置（默认复用 spring.data.redis 配置）
dc.lock.redisson.redis.host=${spring.data.redis.host}
dc.lock.redisson.redis.port=${spring.data.redis.port}
dc.lock.redisson.redis.database=${spring.data.redis.database}
dc.lock.redisson.redis.password=${spring.data.redis.password}
dc.lock.redisson.redis.lazy-initialization=true
dc.lock.redisson.netty.use-virtual-thread=false
```
