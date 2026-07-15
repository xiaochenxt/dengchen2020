---
name: raw-json
description: 需要使用Jackson自带的@JsonRawValue注解对String类型字段进行JSON原样输出时使用。本模块为其增强双向能力，反序列化时可直接接收JSON对象/数组。当用户提到@JsonRawValue、RawDeserializer、原始JSON输出、不转义JSON、JsonRawValue等关键词时使用
---

# 增强 Jackson @JsonRawValue 注解

## 概述

`jackson-ext` 模块直接支持 Jackson 自带的 `@JsonRawValue` 注解。

Jackson 自带的 `@JsonRawValue` 本来只支持序列化（输出时原样输出，不转义），**不支持反序列化（输入）**。

本模块通过 `DcModule` 为其额外注册了反序列化支持，使得带 `@JsonRawValue` 的 String 字段在反序列化时也能直接接收 JSON 对象/数组：

## 使用场景

当 `String` 类型字段中存储的是 JSON 格式数据（JSON 对象 `{}` 或 JSON 数组 `[]`)时:

- **数据库查询结果**: 数据库某字段存储的是 JSON 字符串，需要原样返回给前端
- **第三方 API 响应透传**: 接收外部 JSON 响应中的嵌套 JSON，保持原样输出
- **前端直接提交 JSON**: 前端直接传 JSON 对象/数组，后端能正常接收为 String

## 使用示例

```java
import com.fasterxml.jackson.annotation.JsonRawValue;

public class User {
    private String name;

    @JsonRawValue
    private String extraInfo;  // 存储 JSON 对象: {"age":25,"city":"北京"}

    @JsonRawValue
    private String tags;       // 存储 JSON 数组: ["tag1","tag2"]
}
```

### 序列化输出

```json
{
  "name": "张三",
  "extraInfo": {"age":25,"city":"北京"},
  "tags": ["tag1","tag2"]
}
```

> 注意: `extraInfo` 和 `tags` 输出为原生 JSON，而非转义的字符串。

### 反序列化输入

前端可提交两种格式：

**格式一: 直接传 JSON 对象/数组**
```json
{
  "name": "张三",
  "extraInfo": {"age":25,"city":"北京"},
  "tags": ["tag1","tag2"]
}
```

**格式二: 传已转义的字符串**
```json
{
  "name": "张三",
  "extraInfo": "{\"age\":25,\"city\":\"北京\"}",
  "tags": "[\"tag1\",\"tag2\"]"
}
```

> 两种格式均能成功反序列化为 String 类型。

## 实现原理

### 模块注册

`DcModule` 通过 **JDK SPI** 机制自动注册到 Jackson 的 `ObjectMapper`:

```
META-INF/services/com.fasterxml.jackson.databind.Module
→ io.github.dengchen2020.jackson.DcModule
```

### 序列化

`JsonRawValueAnnotationIntrospector` 检测到 Jackson 自带的 `@JsonRawValue` 注解后，为其注册 Jackson 内置的 `RawSerializer<String>` 输出原生 JSON，不进行转义。

### 反序列化

`JsonRawValueAnnotationIntrospector` 检测到 `@JsonRawValue` 注解后，同时注册 `RawDeserializer`，核心逻辑在 `RawDeserializer.deserialize()`:

```java
public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    if (jp.currentToken().isStructStart())   // 当前 token 是 { 或 [
        return jp.readValueAsTree().toString(); // 读取为 JsonNode 再转字符串
    return jp.getValueAsString();               // 否则按普通字符串处理
}
```

- `jp.currentToken().isStructStart()`: 判断当前 JSON token 是否是结构体开始（`START_OBJECT` 或 `START_ARRAY`）
- `jp.readValueAsTree().toString()`: 将整个结构体读取为 `JsonNode` 树，再转为字符串（保留原始 JSON 格式，但不保留原始缩进）
- `jp.getValueAsString()`: 对普通字符串值直接提取

## 模块结构

```
jackson-ext/src/main/java/io/github/dengchen2020/jackson/
├── RawDeserializer.java                  // 反序列化器
├── JsonRawValueAnnotationIntrospector.java // 注解内省器
└── DcModule.java                         // Jackson 模块

jackson-ext/src/main/resources/META-INF/
└── services/
    └── com.fasterxml.jackson.databind.Module  // SPI 注册
```

## 注意事项

1. 该注解仅对 `String` 类型字段有效，不可用于其他类型
2. `readValueAsTree().toString()` 会丢失原始 JSON 的缩进格式
3. JDK SPI 自动注册要求 `jackson-ext` 在 classpath 中
4. 如需手动注册，也可通过 `ObjectMapper.registerModule(new DcModule())`
5. 使用 Jackson 自带的 `@JsonRawValue` 注解（路径：`com.fasterxml.jackson.annotation.JsonRawValue`），无需额外导入
