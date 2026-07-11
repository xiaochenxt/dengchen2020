ID 生成器，支持雪花算法（Snowflake）多节点自动配置机器 ID，也支持基于 Redis 的递增 ID。

项目中引入 `spring-boot-starter-data-redis` 时会自动启用雪花算法机器 ID 自动配置，不引入则使用手动设置。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

使用方式（Spring 环境会自动初始化）：

```java
IdHelper.nextId();                    // 生成全局唯一ID（long）
IdHelper.nextIdBase62();              // 转为62进制字符串（数字+大小写字母）
IdHelper.nextIdBase36Upper();         // 转为36进制大写字符串（数字+大写字母）
IdHelper.nextIdBase36Lower();         // 转为36进制小写字符串（数字+小写字母）
IdHelper.extractTime(id);             // 从ID中解析时间
IdHelper.newLongFromTimestamp(timestamp); // 根据时间戳生成ID
```

properties 配置：

```properties
dc.id.snowflake.method=1                     # 雪花计算方法（1-漂移算法|2-传统算法），默认1
dc.id.snowflake.base-time=1657209600000      # 基础时间（ms），一旦确定不能再修改
dc.id.snowflake.worker-id=0                  # 机器码，多节点需不同（引入redis后自动分配）
dc.id.snowflake.worker-id-bit-length=6       # 机器码位长，默认6，取值范围[1,15]
dc.id.snowflake.seq-bit-length=6             # 序列数位长，默认6，取值范围[3,21]
dc.id.snowflake.max-seq-number=0             # 最大序列数（含），0表示取最大值
dc.id.snowflake.min-seq-number=5             # 最小序列数（含），默认5，前5个为保留位
dc.id.snowflake.top-over-cost-count=2000     # 最大漂移次数（含），默认2000
```

无 Redis 环境手动初始化：

```java
SnowflakeIdGeneratorOptions options = new SnowflakeIdGeneratorOptions((short) 1);
options.setMethod((short) 1);
// 更多配置...
IdHelper.setIdGenerator(new SnowflakeIdGenerator(options));
IdHelper.nextId(); // 生成ID
```

基于 Redis 的递增 ID 生成器：

```java
@Resource
private StringRedisTemplate stringRedisTemplate;

// 全局唯一递增ID（无业务区分）
RedisIdGenerator idGenerator = new RedisIdGenerator(stringRedisTemplate, null);
long id = idGenerator.newLong();

// 按业务区分的递增ID
RedisIdGenerator orderIdGen = new RedisIdGenerator(stringRedisTemplate, "order");
long orderId = orderIdGen.newLong();
```
