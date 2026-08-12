IP 地址归属地查询，基于 ip2region xdb 数据包，支持 IPv4 和 IPv6。

```xml
<dependency>
    <groupId>io.github.dengchen2020</groupId>
    <artifactId>dc-spring-boot-starter-ip</artifactId>
</dependency>
```

使用方式：

```java
@Resource
private IpService ipService;

// 查询 IP 归属地
IpInfo info = ipService.getInfo("114.114.114.114");
info.ip();          // 114.114.114.114
info.country();     // 中国
info.province();    // 江苏
info.city();        // 南京
info.district();    // 秦淮区
info.isp();         // 电信

// 完整 IpInfo 包含以下字段：
// ip, continent(大洲), country, province, city, district, isp,
// longitude, latitude, areaCode, cityCode, zipCode, timeZone,
// currency, elevation, weatherStation, alpha2Code
```

properties 配置：

```properties
# xdb 数据包文件路径（默认从 classpath 根目录加载）
dc.ip.v4.location=ip.xdb
dc.ip.v6.location=ipv6.xdb

# 是否验证数据包有效性（仅首次需要，验证通过后可改为 false）
dc.ip.verify=false

# 使用 V2 版本（需要 2025 年 10 月后生成的新 xdb 数据包）
dc.ip.use-v2=false

# V2 全内存缓存模式（默认启用）
dc.ip.buffer-cache.enabled=true
# V2 非全缓存模式下的 searcher 数量
dc.ip.v4.searcher-count=20
dc.ip.v6.searcher-count=20
```
