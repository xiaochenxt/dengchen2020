可使用高级 JPA 功能，基于 Querydsl 保障类型安全。

实体类继承功能接口来使用对应能力：

```java
// 1. 定义仓库接口
@Repository
public interface UserRepository extends BaseJpaRepository<User, Long> {
}
```

基础 CRUD（CrudJpaRepository）：

```java
int delete(Iterable<ID> ids);           // 批量删除
int delete(ID... ids);                  // 批量删除
List<T> selectInIds(Iterable<ID> ids);  // 批量查询
List<T> selectInIds(ID... ids);         // 批量查询
boolean exists(ID id);                  // 根据主键判断是否存在
```

悲观锁查询（QueryJpaRepository）：

```java
T selectByIdForUpdate(ID id);                         // 排他锁（阻塞等待）
Optional<T> findByIdForUpdate(ID id);                 // 排他锁
T selectByIdForUpdateNowait(ID id);                   // 排他锁（不等待，失败抛异常）
T selectByIdForShare(ID id);                          // 共享锁
List<T> selectInIdsForUpdateSkipLocked(Iterable<ID>); // 排他锁跳过已锁
```

Querydsl 类型安全查询（QuerydslJpaRepository / QuerydslPagingJpaRepository）：

```java
JPAQuery<T> selectFrom();                                                    // 单表查询
SimplePage<T> findAll(@Nullable Predicate predicate, Page page, OrderSpecifier<?>... o); // 分页查询
SimplePage<T> fetchPage(JPAQuery<R> query, Page page, OrderSpecifier<?>...); // 分页查询
Stream<T> findStream(Predicate, Page, OrderSpecifier<?>...);                 // 流式读取
JPAUpdateClause update(Predicate where);                                     // 更新
long delete(Predicate where);                                                // 删除
```

复杂多条件分页查询（BooleanBuilder + findAll）：

```java
import io.github.dengchen2020.core.jdbc.Page;

public interface GoodsRepository extends BaseJpaRepository<Goods, Long> {

    QGoods q_goods = QGoods.goods;

    default SimplePage<GoodsDTO> list(GoodsQueryParam param) {
        var builder = new BooleanBuilder();
        // 多条件动态拼接
        builder.and(q_goods.shopId.in(getQueryShopIds()));
        if (StringUtils.hasText(param.getName())) builder.and(q_goods.name.contains(param.getName()));
        return findAll(builder, Page.of(1,10), o);
    }
}
```

连表查询（JPAQuery + Projections）：

```java
public interface OrderRepository extends BaseJpaRepository<Order, Long> {

    QOrder q_order = QOrder.order;
    QUser q_user = QUser.user;

    default SimplePage<OrderDTO> list(OrderQueryParam param) {
        var builder = new BooleanBuilder();
        // 多条件动态拼接
        if (param.getUserId() != null) builder.and(q_order.userId.eq(param.getUserId()));
        if (param.getStatus() != null) builder.and(q_order.status.eq(param.getStatus()));
        if (param.getStartTime() != null) builder.and(q_order.createTime.goe(param.getStartTime()));

        // select 投影 + left join 连表
        var query = select(Projections.bean(OrderDTO.class, q_order,
                q_user.name.as("userName"),
                q_user.phone.as("userPhone")
        )).leftJoin(q_user).on(q_order.userId.eq(q_user.id))
          .where(builder);

        return fetchPage(query, Page.of(1,10), q_order.id.desc());
    }
}
```

原生 SQL 查询（QuerydslJdbcRepository）：

```java
JPASQLQuery<Tuple> nativeSelect(Expression<?>... exprs); // 原生 SQL 查询
JPASQLQuery<?> with(EntityPath<?> path, SubQueryExpression<?> query); // 公用表表达式（CTE）
```

租户/用户数据隔离（TenantJpaRepository / UserIdJpaRepository）：

```java
T selectByIdWithTenantId(ID id);           // 携带租户id查询
int deleteWithTenantId(ID id);             // 携带租户id删除
T selectByIdWithUserId(ID id);             // 携带用户id查询
int deleteWithUserId(ID... ids);           // 携带用户id删除
```

EntityManager 管理（EntityManagerRepository）：

```java
void clear();      // 清除持久化上下文
void detach(T entity); // 使实体变为游离状态
```

Querydsl JSON 查询（PostgreSQL jsonb / MySQL json）：

```java
// jsonb 对象查询
JsonbObjectTemplate jsonb = new JsonbObjectTemplate("jsonb_get({0},{1})", field, key);
BooleanExpression expr = jsonb.contains("name", "张三");

// jsonb 路径查询
JsonValueTemplate val = jsonb.get("address", "city");
```

Querydsl 表达式工具（JpaExpressions）：

```java
StringExpression name = JpaExpressions.jsonValue(field, "$.name");  // json_value
BooleanExpression exists = JpaExpressions.jsonExists(field, "$.id"); // json_exists
NumberExpression<Integer> age = JpaExpressions.intValue(field);     // 类型转换
```

自定义投影（Projections）：

```java
// Bean 属性赋值投影
QBean<UserDTO> dto = Projections.bean(UserDTO.class, user.id, user.name);
// 连表查询投影
QBean<OrderDTO> dto = Projections.bean(OrderDTO.class, order, user.name, user.phone);
// Record 构造投影
ConstructorExpression<UserDTO> dto = Projections.constructor(UserDTO.class, user);
```

properties 配置：

```properties
# 是否启用动态 insert sql（默认 true）
dc.jpa.properties.hibernate.dynamic-insert=true
# 是否启用动态 update sql（默认 true）
dc.jpa.properties.hibernate.dynamic-update=true
# 非 dev 环境自动禁用 hibernate 自动建表和启动时命名查询检查
```
