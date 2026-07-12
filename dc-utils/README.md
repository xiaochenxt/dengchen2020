工具类集合，涵盖 JSON、Bean 拷贝、加密解密、摘要算法、日期时间、随机数、验证码、二维码、CSV、S3、图片压缩等。

JSON 处理（JsonUtils）：

```java
String json = JsonUtils.toJson(source);
String json = JsonUtils.toJsonIgnoreNull(source);
User user = JsonUtils.fromJson(json, User.class);
List<User> users = JsonUtils.fromJson(json, new TypeReference<List<User>>(){});
UserDTO dto = JsonUtils.convertValue(source, UserDTO.class);
ObjectNode obj = JsonUtils.createObjectNode();
ArrayNode arr = JsonUtils.createArrayNode();
boolean isJson = JsonUtils.isJsonObjectOrArray(str);
```

Bean 拷贝（BeanUtils）— 基于 CGLib 字节码生成，忽略 null 值：

```java
BeanUtils.copyProperties(source, target);
BeanUtils.copyProperties(source, target, "password");
UserRecord record = BeanUtils.convertValue(user, UserRecord.class);
```

加密解密（AESUtils / RSAUtils）：

```java
String key = AESUtils.generateKey(256);
String iv = AESUtils.generateIV();
String encrypted = AESUtils.encrypt("你好世界", key, iv);
String decrypted = AESUtils.decrypt(encrypted, key, iv);

KeyPair keyPair = RSAUtils.generateKeyPair();
String encrypted = RSAUtils.encrypt("你好世界", keyPair.getPublic());
String decrypted = RSAUtils.decrypt(encrypted, keyPair.getPrivate());
```

摘要算法（DigestUtils）：

```java
String md5 = DigestUtils.md5Hex("hello");
String sha256 = DigestUtils.sha256Hex("hello");
String fileMd5 = DigestUtils.md5Hex(java.io.FileInputStream("/path/file.txt"));
```

日期时间（DateTimeUtils）：

```java
LocalDateTime dt = DateTimeUtils.localDateTime(new Date());
Date date = DateTimeUtils.date(LocalDateTime.now());
LocalDateTime begin = DateTimeUtils.beginOfDay(dt);
LocalDateTime end = DateTimeUtils.endOfDay(dt);
String str = DateTimeUtils.format(dt, "yyyy-MM-dd HH:mm:ss");
Date parsed = DateTimeUtils.parse(str, "yyyy-MM-dd HH:mm:ss");
```

随机数（RandomUtils / RandomStringUtils）— 实例式 API：

```java
String code = RandomStringUtils.insecure().nextNumeric(6);
String str = RandomStringUtils.insecure().nextAlphanumeric(16);
int num = RandomUtils.insecure().randomInt(0, 100);
```

验证码（CaptchaUtils）：

```java
ArithmeticCaptcha captcha = CaptchaUtils.arithmetic();
String base64 = captcha.toBase64();
String code = captcha.text();

ChineseCaptcha chinese = CaptchaUtils.chinese();
```

二维码（QRCodeGenerator）— Builder 模式：

```java
QRCodeGenerator qr = QRCodeGenerator.builder().size(300).build();
qr.generate("https://example.com", outputStream);

QRCodeGenerator qr2 = QRCodeGenerator.builder().size(300).logo(logoBytes).build();
qr2.generate("https://example.com", outputStream);
```

CSV 读写（CsvReader / CsvWriter）：

```java
var headerMap = Map.of("name", "姓名", "age", "年龄");
CsvWriter writer = new CsvWriter(outputStream, headerMap);
writer.writeHeader();
writer.writeRow(Map.of("name", "张三", "age", 25));
writer.close();

CsvReader reader = new CsvReader(inputStream);
reader.forEach(row -> System.out.println(row.get("姓名")));
reader.close();
```

S3 对象存储（S3Service）：

```java
S3Service s3 = new S3Service("https://s3.example.com", "ak", "sk", Region.CN_NORTH_1, null);
String url = s3.uploadFile("bucket", "key", inputStream, contentLength, "image/png");
ResponseInputStream<GetObjectResponse> res = s3.downloadFile("bucket", "key");
s3.deleteFile("bucket", "key");
String downloadUrl = s3.generatePresignedDownloadUrl("bucket", "key", Duration.ofHours(1));
```

图片压缩（ImageUtils）：

```java
ImageUtils.compression(inputStream, 0.8f, false, outputStream);
String base64 = ImageUtils.imgToBase64(new File("/path/image.jpg"), 0.8f, false);
```

其他工具：

```java
String uuid = StrUtils.uuidSimplified();
boolean hasText = StrUtils.hasText(str);
String ip = RequestUtils.getRemoteAddr();
String localIp = IPUtils.getLocalAddr();
long ipNum = IPUtils.ipv4ToLong("192.168.1.1");
```

依赖均为 optional，按需引入对应 maven 依赖即可使用。
