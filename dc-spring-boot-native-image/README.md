直接引入依赖
- Maven:
```xml
<dependency>
    <groupId>io.github.dengchen2020</groupId>
    <artifactId>dc-spring-boot-native-image</artifactId>
    <version>${lastVersion}</version>
</dependency>
```
springboot项目需先执行spring-boot:process-aot，
最后执行native:compile-no-fork既可编译成功。

引入该依赖后，编译后的二进制程序体积可能比之前要大，原因是--enable-http --enable-https 增加6M左右
-H:+AddAllCharsets增加12M左右，不影响性能，只为提高兼容性减少错误率。