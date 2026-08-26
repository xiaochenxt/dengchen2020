---
name: dto-projection
description: 基于selectBean/selectRecord/selectConstructor的DTO投影查询，自动匹配实体同名字段，支持JavaBeans、Record投影与连表额外字段。当用户提到DTO投影、selectBean、selectRecord、selectConstructor、字段自动匹配、连表查询返回DTO、Record投影等关键词时使用
---

# Querydsl DTO 投影查询（selectBean / selectRecord / selectConstructor）

## 概述

`dc-spring-boot-starter-jpa` 模块的 `QuerydslJpaRepository`（`BaseJpaRepository` 继承）提供三个高频投影查询方法，**自动查询实体同名字段**，无需手动罗列字段列表，实体新增/修改字段后 DTO 查询零改动。

| 方法 | 结果类型要求 | 字段匹配规则 | 适用场景 |
|------|-------------|-------------|---------|
| `selectBean` | 符合 JavaBeans 规范（有 setter） | 同名字段自动查询，额外字段需 `.as("属性名")` 匹配 setter | 可变 DTO 投影、连表 |
| `selectRecord` | public Record | 同名字段自动查询；无额外字段时缺失字段补 null | 不可变 Record 投影 |
| `selectConstructor` | 有对应 public 构造函数 | 全手动，与构造函数参数顺序一致 | 聚合、重命名、表达式 |

返回值均为 `JPAQuery<R>`，可继续链式调用 `join`/`where`/`orderBy`，配合 `fetchPage()` 分页。

## 使用场景

- 列表页/详情页只查询 DTO 所需字段，避免 select 全字段
- 连表查询补充关联表字段到 DTO/Record
- Record 不可变 DTO 投影
- 聚合统计投影（count/group by 等）

## 使用示例

### selectBean：单表 DTO 投影

```java
// UserDTO 中 id、name、phone 拥有 setter 且与实体字段同名 → 自动查询
public interface UserRepository extends BaseJpaRepository<User, Long> {

    QUser q_user = QUser.user;

    default List<UserDTO> listActive() {
        return selectBean(UserDTO.class)
                .where(q_user.status.eq(1))
                .fetch();
    }
}
```

### selectBean：连表补充额外字段

```java
default SimplePage<OrderDTO> list(OrderQueryParam param) {
    var builder = new BooleanBuilder();
    if (param.getUserId() != null) builder.and(q_order.userId.eq(param.getUserId()));

    // 同名字段自动查询；关联表字段用 .as() 别名匹配 OrderDTO 的 setter
    var query = selectBean(OrderDTO.class,
            q_user.name.as("userName"),
            q_user.phone.as("userPhone"))
        .leftJoin(q_user).on(q_order.userId.eq(q_user.id))
        .where(builder);

    return fetchPage(query, param, q_order.id.desc());
}
```

### selectRecord：Record 投影

```java
// Record 组件 id、name、phone 与实体字段同名 → 自动查询
// 无额外字段时，实体中不存在的组件自动补 null
public record UserInfo(Long id, String name, String phone) {}

default List<UserInfo> listUserInfo() {
    return selectRecord(UserInfo.class)
            .where(q_user.deleted.isFalse())
            .fetch();
}
```

### selectRecord：连表补充额外字段

```java
// 实体同名字段在前（无顺序要求），额外字段必须在 Record 末尾且与 exprs 顺序一致
public record OrderInfo(Long id, Integer status, String userName) {}

default List<OrderInfo> listOrderInfo() {
    return selectRecord(OrderInfo.class, q_user.name)
            .leftJoin(q_user).on(q_order.userId.eq(q_user.id))
            .fetch();
}
```

### selectConstructor：手动构造函数投影

```java
// exprs 顺序必须与某个 public 构造函数的参数顺序一致，适合聚合、字段重命名、表达式场景
public record StatusCount(Integer status, long total) {}

default List<StatusCount> countByStatus() {
    return selectConstructor(StatusCount.class, q_order.status, q_order.count())
            .groupBy(q_order.status)
            .fetch();
}
```

## 实现原理

```
selectBean(type, exprs)
└── QuerydslUtils.bean()   → 反射读取 type 的 setter，匹配实体 Q 类同名字段
                             生成 Projections.bean(type, 同名字段 + exprs)
selectRecord(type, exprs)
└── QuerydslUtils.record() → 读取 Record 组件，匹配实体 Q 类同名字段
                             生成 Projections.constructor(type, 同名字段 + exprs)
selectConstructor(type, exprs)
└── Projections.constructor(type, exprs)（Querydsl 原生，全手动）
```

- 字段匹配结果通过 `ConcurrentReferenceHashMap` 缓存，反射开销只发生一次
- `selectBean` 基于 `Projections.bean()`，按表达式**别名**匹配 setter，因此额外字段必须 `.as("属性名")`
- `selectRecord`/`selectConstructor` 基于构造函数投影，按**位置顺序**匹配，无需别名

## 注意事项

1. `selectBean` 的目标类必须符合 JavaBeans 规范（有 setter）；额外字段必须 `.as("属性名")` 与 setter 名对应
2. `selectRecord` 的 type 必须为 **public Record**（非 public 时其规范构造函数也不是 public，构造函数投影无法访问）；无额外字段时，实体中不存在的 Record 组件自动补 null（支持 String、数值、Boolean、BigDecimal、BigInteger 等常见类型）；有额外字段时，实体同名字段声明在额外字段之前（之间顺序不限），额外字段在末尾且与 exprs 顺序一致
3. `selectConstructor` 不做任何自动匹配，exprs 数量、顺序、类型必须与某个 public 构造函数完全一致
4. 三个方法自动 `from` 当前实体表，连表用 `leftJoin().on()`，不要再 `from` 其他表（会形成笛卡尔积）
5. 需要分页时将返回的 `JPAQuery<R>` 传给 `fetchPage(query, page, order)`
