---
name: json-utils
description: JSON处理工具，提供序列化、反序列化、类型转换、JSON节点操作、JSON合法性校验等功能。当用户提到JsonUtils、JSON转换、toJson、fromJson、JSON解析、JSON序列化、ObjectNode、ArrayNode、JSON校验等关键词时使用
---

# JSON 处理工具

## 概述

`dc-utils` 模块的 `JsonUtils` 提供便捷的 JSON 处理能力，基于 Jackson 实现。包含序列化、反序列化、类型转换、JSON 节点操作、流式校验等功能。

| 功能 | 方法 | 说明 |
|------|------|------|
| **序列化** | `toJson()` / `toJsonIgnoreNull()` | 对象转 JSON 字符串 |
| **反序列化** | `fromJson()` | JSON 字符串转对象 |
| **类型转换** | `convertValue()` | 对象之间按 JSON 语义转换 |
| **节点操作** | `createObjectNode()` / `createArrayNode()` | 构建/操作 JSON 树 |
| **JSON 校验** | `isJsonObjectOrArray()` / `isJsonObject()` / `isJsonArray()` | 流式校验，高性能 |
| **字节序列化** | `serialize()` / `deserialize()` | 对象 ↔ 字节数组 |

## 使用场景

- 对象与 JSON 字符串互转
- 构建动态 JSON 结构（ObjectNode/ArrayNode）
- JSON 合法性验证（接收外部数据时校验）
- 带类型的泛型反序列化（`TypeReference`）

## 使用示例

### 基本序列化/反序列化

```java
// 对象转JSON
String json = JsonUtils.toJson(user);
String json = JsonUtils.toJsonIgnoreNull(user); // 忽略null属性

// JSON转对象
User user = JsonUtils.fromJson(json, User.class);

// 泛型反序列化
List<User> users = JsonUtils.fromJson(json, new TypeReference<List<User>>(){});

// 对象间按JSON语义转换
UserDTO dto = JsonUtils.convertValue(user, UserDTO.class);
```

### JSON 节点操作

```java
// 构建 JSON 对象
ObjectNode obj = JsonUtils.createObjectNode();
obj.put("name", "张三");
obj.put("age", 25);
ObjectNode address = obj.putObject("address");
address.put("city", "北京");
address.put("district", "海淀");

// 构建 JSON 数组
ArrayNode arr = JsonUtils.createArrayNode();
arr.add("item1");
arr.add("item2");

// 解析 JSON 树
JsonNode root = JsonUtils.readTree(json);
JsonNode name = root.get("name");
```

### JSON 合法性校验

```java
// 校验是否为合法的 JSON 对象或数组（流式校验，性能好）
boolean isValid = JsonUtils.isJsonObjectOrArray(str);

// 校验是否为 JSON 对象（{...}）
boolean isObj = JsonUtils.isJsonObject(str);

// 校验是否为 JSON 数组（[...]）
boolean isArr = JsonUtils.isJsonArray(str);
```

### 字节序列化

```java
// 对象 → 字节数组
byte[] data = JsonUtils.serialize(user);

// 字节数组 → 对象
User user = JsonUtils.deserialize(data, User.class);
```

## 实现原理

```
JsonUtils (静态工具入口)
    ↓ 委托
JsonHelper (Spring Bean，与 Spring Jackson 配置一致)
    ↓
Jackson ObjectMapper (已通过 Spring Boot 自动配置)
    ├── 序列化: writeValueAsString() / writeValueAsBytes()
    ├── 反序列化: readValue() / readValueAsTree()
    ├── 转换: convertValue()
    └── 校验: JsonParser 流式遍历 (不构建对象树)
```

- `JsonHelper` 在 `JacksonAutoConfiguration` 中注入，与 Spring 的全局 Jackson 配置一致
- 流式校验 `isJsonObjectOrArray()` 使用 `JsonParser` 只遍历不构建，性能远高于 `readTree()`

## 模块结构

```
dc-utils/src/main/java/io/github/dengchen2020/core/utils/
├── JsonUtils.java      // 静态工具类（对外入口）
└── JsonHelper.java     // Jackson 封装（Spring Bean）
```

## 注意事项

1. `JsonUtils` 是静态工具类，底层委托给 `JsonHelper` Bean 执行，需确保 Spring 上下文已初始化
2`toJsonIgnoreNull()` 在序列化时会忽略所有值为 null 的属性
3`isJsonObjectOrArray()` 使用流式解析，不会反序列化为完整对象树，性能极高，适合批量校验
