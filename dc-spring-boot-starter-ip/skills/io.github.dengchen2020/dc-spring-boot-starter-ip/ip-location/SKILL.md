---
name: ip-location
description: IP地址归属地查询，基于ip2region xdb数据包，支持IPv4和IPv6查询。当用户提到IP归属地、IP查询、IpService、IP定位、IP地址解析、ip2region等关键词时使用
---

# IP 地址归属地查询

## 概述

`dc-spring-boot-starter-ip` 模块提供 IP 地址归属地查询能力，基于 ip2region 的 xdb 数据包实现，支持 IPv4 和 IPv6，支持 v1 和 v2 两种数据格式。

| 特性 | 说明 |
|------|------|
| **查询方式** | `IpService.getInfo(ip)` 一键查询 |
| **返回值** | `IpInfo` Record，含大洲/国家/省份/城市/区县/ISP/经纬度等 |
| **数据格式** | 支持 v1（传统 xdb）和 v2（2025 年 10 月后新格式） |
| **IPv6** | 支持 IPv6 地址查询 |
| **缓存** | V2 支持全内存缓存模式，性能更高 |

## 使用场景

- 用户登录 IP 归属地展示
- 订单收货地址 IP 校验
- 操作日志记录 IP 属地
- 访问来源统计分析

## 使用示例

### 基本查询

```java
@Resource
private IpService ipService;

IpInfo info = ipService.getInfo("114.114.114.114");
info.ip();           // 114.114.114.114
info.country();      // 中国
info.province();     // 江苏
info.city();         // 南京
info.district();     // 秦淮区
info.isp();          // 电信
```

### 完整 IpInfo 字段

```java
IpInfo(
    String ip,              // IP 地址
    String continent,       // 大洲（如 Asia）
    String country,         // 国家（如 中国）
    String province,        // 省份（如 江苏）
    String city,            // 城市（如 南京）
    String district,        // 区县（如 秦淮区）
    String isp,             // 运营商（如 电信）
    String longitude,       // 经度
    String latitude,        // 纬度
    String areaCode,        // 行政区码
    String cityCode,        // 电话区号
    String zipCode,         // 邮编
    String timeZone,        // 时区
    String currency,        // 国家英文缩写
    String elevation,       // 海拔
    String weatherStation,  // 气象站
    String alpha2Code       // 国家二位简称
)
```

### 查询 IPv6

```java
IpInfo info = ipService.getInfo("2001:4860:4860::8888");
```

### 结合 RequestUtils 使用

```java
import io.github.dengchen2020.core.utils.RequestUtils;
import io.github.dengchen2020.core.utils.IPUtils;

@Resource
private IpService ipService;

// 从请求中获取客户端 IP
String ip = RequestUtils.getRemoteAddr(request);
// 或取本机 IP
String localIp = IPUtils.getLocalAddr();

IpInfo info = ipService.getInfo(ip);
```

## 配置说明

```properties
# xdb 数据包文件路径（默认从 classpath 根目录加载）
dc.ip.v4.location=ip.xdb
dc.ip.v6.location=ipv6.xdb

# 首次使用建议设为 true 验证数据包有效性，通过后改为 false
dc.ip.verify=false

# 使用 V2 版本（需 2025 年 10 月后生成的新 xdb 数据包）
dc.ip.use-v2=false

# V2 全内存缓存模式（默认启用，查询性能最高）
dc.ip.buffer-cache.enabled=true
# V2 非全缓存模式下的 searcher 数量
dc.ip.v4.searcher-count=20
dc.ip.v6.searcher-count=20
```

## 实现原理

```
应用启动 → IpAutoConfiguration
    ↓
读取 dc.ip.* 配置
    ↓ use-v2=false           ↓ use-v2=true
IpXdbServiceImpl              IpXdbV2ServiceImpl
    │                           ├── bufferCache=true → 全量加载到内存
    │                           └── bufferCache=false → 多 searcher 并发查询
    ↓
初始化 xdb searcher
    ↓
getInfo(ip) → searcher.search(ip) → 解析结果 → 返回 IpInfo
```

- v1：基于 `IpXdbServiceImpl`，使用 ip2region 原生的 xdb searcher
- v2：基于 `IpXdbV2ServiceImpl`，支持全内存缓存和多 searcher 并发，适合高并发场景

## 模块结构

```
dc-spring-boot-starter-ip/src/main/java/io/github/dengchen2020/ip/
├── config/
│   └── IpAutoConfiguration.java       // 自动配置
├── model/
│   └── IpInfo.java                    // IP 信息 Record
├── service/
│   ├── IpService.java                 // 查询接口
│   └── impl/
│       ├── dat/                       // dat 格式支持（旧版）
│       │   ├── IpDatServiceImpl.java
│       │   ├── IPLocation.java
│       │   └── Location.java
│       └── xdb/                       // xdb 格式支持
│           ├── IpXdbServiceImpl.java  // v1 实现
│           ├── IpXdbV2ServiceImpl.java// v2 实现
```

## 注意事项

1. 需要自行准备 xdb 数据包（从 [ip2region](https://github.com/lionsoul2014/ip2region) 项目下载），默认从 classpath 根目录加载 `ip.xdb` 和 `ipv6.xdb`
2. `dc.ip.verify=true` 仅在首次或更换数据包时需要，验证通过后建议改为 `false` 以加快启动速度
3. V2 版本需要 2025 年 10 月后生成的新 xdb 数据包，旧数据包不兼容
4. 全内存缓存模式（V2 默认）将整个 xdb 加载到内存，查询性能最高但占用内存（约几十 MB）
