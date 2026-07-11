直接引入依赖
- Maven:
```xml
<dependency>
    <groupId>io.github.dengchen2020</groupId>
    <artifactId>dc-spring-boot-native-image</artifactId>
    <version>${lastVersion}</version>
</dependency>
```
windows需要安装编译工具（linux不需要），https://www.graalvm.org/latest/getting-started/windows/#prerequisites-for-native-image-on-windows  </br>
1.确保有maven并配置好环境变量 </br>
2.下载并配置好环境变量，GRAALVM_HOME https://www.graalvm.org/downloads/# </br>
3.springboot项目需先执行spring-boot:process-aot，
最后执行native:compile-no-fork既可编译成功。

引入该依赖后，编译后的二进制程序体积可能比之前要大，原因是--enable-http --enable-https 增加6M左右
-H:+AddAllCharsets增加12M左右，不影响性能，只为提高兼容性减少错误率。