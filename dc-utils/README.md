工具类集合，涵盖 JSON、Bean 拷贝、加密解密、摘要算法、日期时间、随机数、验证码、二维码、CSV、S3、图像处理等。

JSON 处理（JsonUtils）：

```java
// 对象转JSON
String json = JsonUtils.toJson(source);
String json = JsonUtils.toJsonIgnoreNull(source); // 忽略null属性

// JSON转对象
User user = JsonUtils.fromJson(json, User.class);
List<User> users = JsonUtils.fromJson(json, new TypeReference<List<User>>(){});

// 类型转换
UserDTO dto = JsonUtils.convertValue(source, UserDTO.class);

// JSON节点操作
ObjectNode obj = JsonUtils.createObjectNode();
obj.put("name", "张三");
ArrayNode arr = JsonUtils.createArrayNode();
arr.add("item1");

// JSON合法性校验（流式校验，高性能）
boolean isJson = JsonUtils.isJsonObjectOrArray(str);
```

Bean 拷贝（BeanUtils）— 忽略 null 值的高性能拷贝：

```java
// Bean → Bean（忽略null属性）
BeanUtils.copyProperties(source, target);

// Bean → Bean（忽略指定字段）
BeanUtils.copyProperties(source, target, "password", "salt");

// Bean → Record（创建新的Record实例）
UserRecord record = BeanUtils.convertValue(user, UserRecord.class);

// Bean → Record（带忽略字段）
UserRecord record = BeanUtils.convertValue(user, UserRecord.class, "password");

// 自定义类型转换器
BeanUtils.copyProperties(source, target, (value, targetType, context, fieldName) -> {
    if (value instanceof String && targetType == Integer.class) {
        return Integer.parseInt((String) value);
    }
    return value;
});
```

加密解密（AESUtils / RSAUtils）：

```java
// AES 加密解密
String key = AESUtils.generateKeyHex();
String encrypted = AESUtils.encryptHex("你好世界", key);
String decrypted = AESUtils.decryptHex(encrypted, key);

// RSA 加密解密
KeyPair keyPair = RSAUtils.generateKeyPair();
String encrypted = RSAUtils.encryptHex("你好世界", keyPair.getPublic());
String decrypted = RSAUtils.decryptHex(encrypted, keyPair.getPrivate());

// RSA 签名验签
String sign = RSAUtils.signHex(data, keyPair.getPrivate());
boolean verify = RSAUtils.verifySignHex(data, keyPair.getPublic(), sign);
```

摘要算法（DigestUtils）— 支持 MD5/SHA-1/SHA-256/SHA-512 等：

```java
String md5 = DigestUtils.md5Hex("hello");
String sha256 = DigestUtils.sha256Hex("hello");

// 文件摘要
String fileMd5 = DigestUtils.md5Hex(new File("/path/file.txt"));
```

日期时间（DateTimeUtils）：

```java
LocalDateTime dt = DateTimeUtils.localDateTime(new Date());
Date date = DateTimeUtils.date(LocalDateTime.now());

LocalDateTime begin = DateTimeUtils.beginOfDay(dt);       // 当天开始
LocalDateTime end = DateTimeUtils.endOfDay(dt);           // 当天结束
LocalDateTime nextDay = DateTimeUtils.beginOfNextDay(dt); // 第二天开始

// 格式化
String str = DateTimeUtils.format(dt, "yyyy-MM-dd HH:mm:ss");
LocalDateTime parsed = DateTimeUtils.parse(str, "yyyy-MM-dd HH:mm:ss");

// 时区转换
ZonedDateTime shanghai = DateTimeUtils.toZone(dt, ZoneId.of("Asia/Shanghai"));
```

随机数（RandomUtils / RandomStringUtils）：

```java
String code = RandomStringUtils.randomNumeric(6);           // 6位数字
String str = RandomStringUtils.randomAlphanumeric(16);       // 16位字母数字
int num = RandomUtils.randomInt(0, 100);                     // 0-100随机整数
```

验证码（CaptchaUtils）：

```java
// 生成图片验证码（算术表达式）
CaptchaUtils.Captcha captcha = CaptchaUtils.createArithmetic();
String base64 = captcha.toBase64();  // 图片Base64
String code = captcha.code();        // 验证码结果

// 生成中文验证码
CaptchaUtils.Captcha captcha = CaptchaUtils.createChinese();
```

二维码（QRCodeGenerator）：

```java
// 生成二维码图片
BufferedImage image = QRCodeGenerator.generate("https://example.com", 300, 300);
// 生成带Logo的二维码
BufferedImage image = QRCodeGenerator.generate("https://example.com", 300, 300, logoFile);
// 解析二维码
String text = QRCodeGenerator.parse(image);
```

CSV 读写（CsvReader / CsvWriter）：

```java
// 写入CSV
CsvWriter writer = new CsvWriter("/path/file.csv");
writer.writeHeader("姓名", "年龄", "邮箱");
writer.writeRow("张三", "25", "zhangsan@example.com");
writer.close();

// 读取CSV
CsvReader reader = new CsvReader("/path/file.csv");
reader.readRows(row -> System.out.println(row.getField("姓名")));
reader.close();
```

S3 对象存储（S3Service）：

```java
@Resource
private S3Service s3Service;

s3Service.putObject("bucket", "key", file.getBytes());
byte[] data = s3Service.getObject("bucket", "key");
s3Service.deleteObject("bucket", "key");
URL presignedUrl = s3Service.presignedGetObject("bucket", "key", 3600);
```

图像处理（ImageUtils / ImageProcessor）：

```java
// 缩放图片
BufferedImage scaled = ImageUtils.scale(image, 800, 600);
// 裁剪
BufferedImage cropped = ImageUtils.crop(image, x, y, w, h);
```

其他工具：

```java
// Base62/Base36 编码
String encoded = Base62Utils.encode(12345L);
long decoded = Base62Utils.decode(encoded);

// IP 工具
String ip = IPUtils.getRemoteAddr(request);
boolean internal = IPUtils.isInternalIp(ip);

// 手机号工具
boolean valid = PhoneNumberUtils.isValidNumber("13800138000", "CN");
PhoneNumberUtils.PhoneNumberInfo info = PhoneNumberUtils.parse("13800138000", "CN");

// 字符串工具
String uuid = StrUtils.uuidSimplified();
boolean blank = StrUtils.isBlank(str);
String masked = StrUtils.mask("13800138000", 3, 4); // 138****8000

// 版本比较
int result = VersionUtils.compareVersion("2.1.0", "2.0.0"); // 1

// 布隆过滤器
BloomFilter filter = new LocalBloomFilter(10000, 0.01);
filter.add("item1");
boolean exists = filter.mightContain("item1");

// 语言检测
Lang lang = LangDetector.detect("Hello world"); // Lang.EN
```

依赖均为 optional，按需引入对应的 maven 依赖即可用量。
