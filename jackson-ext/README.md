Jackson 扩展，直接支持 Jackson 自带的 `@JsonRawValue` 注解。

Jackson 自带的 `@JsonRawValue` 已支持序列化（输出时原样输出，不转义），但**默认不支持反序列化（输入）**。

本模块通过 `DcModule` 为其额外注册了反序列化支持，使得带 `@JsonRawValue` 的 String 字段在反序列化时也能直接接收 JSON 对象/数组。

```xml
<dependency>
    <groupId>io.github.dengchen2020</groupId>
    <artifactId>jackson-ext</artifactId>
</dependency>
```

使用方式一：注册 `DcModule` 到 Jackson ObjectMapper

```java
@Bean
public ObjectMapper objectMapper() {
    return JsonMapper.builder()
            .addModule(new DcModule())
            .build();
}
```

使用方式二：通过 JDK SPI 自动注册（引入依赖后 Jackson ObjectMapper 自动发现 `META-INF/services/com.fasterxml.jackson.databind.Module`）

实体类中使用：

```java
import com.fasterxml.jackson.annotation.JsonRawValue;

public class Product {
    private Long id;
    private String name;

    @JsonRawValue
    private String attributes; // 存储 JSON 字符串
}
```

序列化效果：

```java
Product product = new Product();
product.setId(1L);
product.setName("商品");
product.setAttributes("{\"color\":\"red\",\"size\":\"L\"}");

String json = objectMapper.writeValueAsString(product);
// 输出：{"id":1,"name":"商品","attributes":{"color":"red","size":"L"}}
// attributes 字段的值原样输出，不会被转义为字符串
```

反序列化效果：

```java
String json = "{\"id\":1,\"name\":\"商品\",\"attributes\":{\"color\":\"red\",\"size\":\"L\"}}";
Product product = objectMapper.readValue(json, Product.class);
// attributes 字段自动将 JSON 对象/数组反序列化为 String
// product.attributes() = "{\"color\":\"red\",\"size\":\"L\"}"
```

典型场景：数据库中 JSON 类型字段映射为 String 类型时，读写都保持 JSON 格式，避免多余的双引号转义。
