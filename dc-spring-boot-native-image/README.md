直接引入依赖
- Maven:
```xml
<dependency>
    <groupId>io.github.dengchen2020</groupId>
    <artifactId>dc-spring-boot-native-image</artifactId>
    <version>${lastVersion}</version>
</dependency>
```
springboot项目需先执行spring-boot:process-aot
最后执行native:compile-no-fork既可编译成功
