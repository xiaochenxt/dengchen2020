properties配置示例： </br>
caffeine（默认使用）:
```
spring.cache.type=caffeine
dc.cache.caffeine.specs.name1.expire-time=90s # 缓存过期时间
dc.cache.caffeine.specs.name1.max=1000 # 缓存最大数量
dc.cache.caffeine.specs.name2.expire-time=30m # 缓存过期时间
dc.cache.caffeine.specs.name2.max=2000 # 缓存最大数量
```
redis:
```
spring.cache.type=redis
dc.cache.redis.specs.name1.expire-time=90s # 缓存过期时间
```
使用caffeine缓存时，当项目中引入了
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```
依赖时，@CacheEvict会通过redis的发布订阅清除其他节点的本地缓存。

编程方式移除缓存：
```
@Resource CacheHelper cacheHelper;
cacheHelper.evict("cacheName", "key"); # 移除指定缓存名下指定key的缓存
cacheHelper.clear("cacheName"); # 清空指定缓存名下的所有缓存
cacheHelper.clearAll(); # 清空所有缓存
```