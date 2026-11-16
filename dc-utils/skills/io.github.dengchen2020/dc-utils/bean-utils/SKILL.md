---
name: bean-utils
description: 高效Bean拷贝工具，基于CGLib字节码生成，自动忽略null值，支持Bean→Bean和Bean→Record转换。当用户提到Bean拷贝、BeanUtils、copyProperties、Record转换、convertValue、属性复制、对象转换等关键词时使用
---

# Bean 拷贝工具

## 概述

`dc-utils` 模块的 `BeanUtils` 提供高性能的 Bean 属性拷贝能力。基于 CGLib 字节码生成，性能远高于 Spring 的反射拷贝，自动忽略源对象中的 null 属性。

| 特性 | BeanUtils | Spring BeanUtils | MapStruct |
|------|-----------|-----------------|-----------|
| **机制** | 字节码生成 | 反射 | 编译期代码生成 |
| **null 处理** | 自动忽略 | 全量拷贝 | 需配置 |
| **Bean→Record** | ✅ 支持 | ❌ 不支持 | ✅ 支持 |
| **使用便捷** | 零配置 | 零配置 | 需定义 Mapper 接口 |

## 使用场景

- DTO / VO / Entity 之间的属性拷贝
- Controller 层入参转 Service 层对象
- PO 转 Record/DTO 返回给前端
- 批量数据转换

## 使用示例

### Bean → Bean（忽略 null）

```java
BeanUtils.copyProperties(source, target);
// source 中为 null 的属性不会覆盖 target 中已有的值
```

### Bean → Bean（忽略指定字段）

```java
BeanUtils.copyProperties(source, target, "password", "salt");
```

### Bean → Record

```java
// 将 PO 转为 Record DTO，null 属性保持 Record 零值
UserRecord record = BeanUtils.convertValue(user, UserRecord.class);
```

### Bean → Record（忽略指定字段）

```java
UserRecord record = BeanUtils.convertValue(user, UserRecord.class, "password");
```

## 实现原理

```
BeanUtils.copyProperties(source, target)
    ↓
getBeanCopier(source.class, target.class, converter)
    ↓ CACHE 中获取或创建
BeanCopier.create(source, target, useConverter)
    ↓ CGLib 生成字节码
生成 copier 子类
    ↓
copier.copy(source, target, converter)
    ↓
逐属性：getter → null 检查 → setter
```

### Bean → Record 流程

```
BeanUtils.convertValue(source, RecordClass)
    ↓
getRecordCopier(source.class, target.class, source, converter)
    ↓ CACHE 中获取或创建
RecordCopier.create(source, target, useConverter)
    ↓ CGLib 字节码
生成 copier 子类
    ↓
遍历 Record 组件(components)
    ↓ 从 source 找同名 getter
    ↓ null 检查 → 调用 Record 构造器
返回 Record 实例
```

- 使用 `ConcurrentReferenceHashMap` 缓存生成的 Copier 实例
- 原生镜像(AOT)模式下回退到 Spring 的 `BeanUtils` 反射拷贝

## 模块结构

```
dc-utils/src/main/java/io/github/dengchen2020/core/utils/bean/
├── BeanUtils.java       // 入口（copyProperties / convertValue）
├── BeanCopier.java      // Bean→Bean 字节码生成
├── RecordCopier.java    // Bean→Record 字节码生成
└── Converter.java       // 类型转换器接口
```

## 注意事项

1. 源对象属性必须有 getter 方法，目标 Bean 必须有对应的 setter 方法
2. 源对象与被拷贝目标类型须匹配或兼容（通过自定义 Converter 处理不兼容类型）
3. `copyProperties()` 不能拷贝到 Record 实例，需使用 `convertValue()`
4. 方法名中带有 `convertValue` 的是 Bean→Record 转换，`copyProperties` 是 Bean→Bean 拷贝
