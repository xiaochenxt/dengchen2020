---
name: querydsl-pagination
description: 基于Querydsl的复杂多条件分页查询，使用BooleanBuilder动态拼接条件，支持JSON字段条件查询。当用户提到Querydsl分页、BooleanBuilder多条件查询、JPA分页条件组合、复杂查询动态条件等关键词时使用
---

# Querydsl 复杂多条件分页查询

## 概述

`dc-spring-boot-starter-jpa` 模块的 `BaseJpaRepository` 提供了基于 Querydsl 的类型安全分页查询能力。结合 `BooleanBuilder` 动态拼接条件，可以轻松实现复杂多条件分页查询。

| 特性 | 说明 |
|------|------|
| **类型安全** | 字段名通过 Q 类编译期校验，修改遗漏可在编译期提示 |
| **动态条件** | `BooleanBuilder` 按需拼接条件，无需写 JPQL |
| **JSON 查询** | 支持 PostgreSQL jsonb / MySQL json 字段条件查询 |
| **一键分页** | `findAll(builder, param, order)` 自动处理分页逻辑 |

## 使用场景

- 后台管理列表页的多条件筛选查询
- 移动端列表带筛选条件的分页加载
- 含 JSONB 字段的复杂条件过滤
- 需要组合排序的多表或多条件分页查询

## 使用示例

### 简单多条件分页

```java
public interface UserRepository extends BaseJpaRepository<User, Long> {

    QUser q_user = QUser.user;

    default SimplePage<User> list(UserQueryParam param, OrderSpecifier<?>... o) {
        var builder = new BooleanBuilder();
        // 多条件动态拼接
        builder.and(q_user.deleted.isFalse());
        if (StringUtils.hasText(param.getName())) {
            builder.and(q_user.name.contains(param.getName()));
        }
        if (param.getStatus() != null) {
            builder.and(q_user.status.eq(param.getStatus()));
        }
        if (param.getStartTime() != null) {
            builder.and(q_user.createTime.goe(param.getStartTime()));
        }
        // 分页查询，param 继承 Page，支持排序
        return findAll(builder, param, o);
    }
}
```

### 含 JSONB 字段的条件查询

```java
default SimplePage<GoodsDTO> list(GoodsQueryParam param, OrderSpecifier<?>... o) {
    var builder = new BooleanBuilder();
    builder.and(q_goods.shopId.in(getQueryShopIds()));
    if (StringUtils.hasText(param.getName())) {
        builder.and(q_goods.name.contains(param.getName()));
    }
    if (param.getCategoryId() != null) {
        // JSONB 数组包含查询
        builder.and(JsonbPath.of(q_goods.category).contains(List.of(param.getCategoryId())));
    }
    if (param.getIsSale() != null) {
        builder.and(q_goods.isSale.eq(param.getIsSale()));
    }
    return findAll(builder, param, o);
}
```

### 连表查询 + 分页

```java
default SimplePage<OrderDTO> list(OrderQueryParam param, OrderSpecifier<?>... o) {
    var builder = new BooleanBuilder();
    if (param.getUserId() != null) {
        builder.and(q_order.userId.eq(param.getUserId()));
    }
    if (param.getStatus() != null) {
        builder.and(q_order.status.eq(param.getStatus()));
    }

    // select 投影 + left join 连表
    var query = select(Projections.bean(OrderDTO.class,
            q_order.id, q_order.status, q_order.amount,
            q_order.createTime,
            q_user.name.as("userName"),
            q_user.phone.as("userPhone")
    )).leftJoin(q_user).on(q_order.userId.eq(q_user.id))
      .where(builder);

    return fetchPage(query, param, o);
}
```

## 实现原理

```
BaseJpaRepository<T, ID> (统一接口)
├── QuerydslPagingJpaRepository<T>  — fetchPage(), findAll(), findStream()
├── QuerydslJpaRepository<T>        — select(), update(), delete()
├── QueryJpaRepository<T, ID>       — 悲观锁查询
├── CrudJpaRepository<T, ID>        — 批量删除/查询
└── EntityManagerRepository<T>      — clear/detach

BaseJpaRepositoryExecutor 实现类
└── 基于 SimpleJpaRepository + Querydsl 增强
```

- `BooleanBuilder` 是 Querydsl 提供的 `Predicate` 实现，支持链式 `and`/`or` 拼接
- `findAll(Predicate, Page, OrderSpecifier[])` 自动执行 count 查询 + 数据查询
- `fetchPage(JPAQuery, Page, OrderSpecifier[])` 适用于自定义查询（连表、投影等）

## 模块结构

```
dc-spring-boot-starter-jpa/src/main/java/io/github/dengchen2020/jpa/
├── config/
│   ├── EnableDcJpaRepositories.java     // 启用注解
│   └── JpaAutoConfiguration.java         // 自动配置
├── base/
│   ├── BaseJpaRepository.java            // 统一接口
│   ├── CrudJpaRepository.java            // CRUD
│   ├── QueryJpaRepository.java           // 悲观锁
│   ├── QuerydslJpaRepository.java        // Querydsl
│   ├── QuerydslPagingJpaRepository.java  // 分页
│   └── BaseJpaRepositoryExecutor.java    // 实现
├── querydsl/
│   ├── Projections.java                  // 投影工具
│   ├── JpaExpressions.java               // 表达式工具
│   └── jsonb/                            // JSONB查询
└── hibernate/
    └── DcFunctionContributor.java        // 数据库函数注册
```

## 注意事项

1. `BooleanBuilder` 中的每个条件默认是 **AND** 关系，多个条件用 `or()` 方法切换为 OR
2. `findAll()` 会先执行 count 查询，如果不需要总条数，设置 `Page.isSelectCount() = false` 可提升性能（推荐基于索引 ID 分页时使用）
3. `Projections.bean()` 要求目标类符合 JavaBeans 规范；Record 类型使用 `Projections.constructor()`
