---
name: digest-utils
description: 摘要算法工具类，支持MD5、SHA-1、SHA-256、SHA-512等，支持文件、字节数组、输入流等数据源。当用户提到摘要算法、MD5、SHA、消息摘要、DigestUtils、md5Hex、sha256Hex、文件指纹等关键词时使用
---

# 摘要算法工具

## 概述

`dc-utils` 模块的 `DigestUtils` 提供标准摘要算法封装，支持多种算法和多种数据源，覆盖字符串、字节数组、文件、输入流、FileChannel 等。

| 算法 | 方法 | 摘要长度 |
|------|------|---------|
| **MD2** | `md2Hex()` | 32 字符 |
| **MD5** | `md5Hex()` | 32 字符 |
| **SHA-1** | `sha1Hex()` | 40 字符 |
| **SHA-256** | `sha256Hex()` | 64 字符 |
| **SHA-384** | `sha384Hex()` | 96 字符 |
| **SHA-512** | `sha512Hex()` | 128 字符 |

## 使用场景

- 文件完整性校验（文件 MD5 / SHA256）
- 密码摘要存储（配合加盐使用）
- 数据指纹 / 防篡改校验
- 接口签名参数

## 使用示例

### 字符串摘要

```java
String md5 = DigestUtils.md5Hex("hello");
String sha256 = DigestUtils.sha256Hex("hello");
String sha512 = DigestUtils.sha512Hex("hello");
```

### 文件摘要

```java
String fileMd5 = DigestUtils.md5Hex(new FileInputStream("/path/file.txt"));
String fileSha256 = DigestUtils.sha256Hex(new FileInputStream("/path/file.zip"));
```

### 字节数组 / 输入流

```java
byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
String md5 = DigestUtils.md5Hex(data);

InputStream is = new FileInputStream("/path/file.bin");
String sha256 = DigestUtils.sha256Hex(is);
```

### 自定义算法

```java
byte[] digest = DigestUtils.digest(
    MessageDigest.getInstance("SHA3-256"), data);
String hex = HexFormat.of().formatHex(digest);
```

## 实现原理

```
DigestUtils.md5Hex(data)
    ↓
getMd5Digest() → MessageDigest.getInstance("MD5")
    ↓
update(data) → digest()
    ↓
bytes → Hex String
```

- 支持多种数据源（`byte[]`、`ByteBuffer`、`File`、`InputStream`、`Path`、`RandomAccessFile`）
- 文件读取使用 NIO `FileChannel` 或缓冲流，大文件性能好
- 所有方法基于标准 `java.security.MessageDigest`

## 模块结构

```
dc-utils/src/main/java/io/github/dengchen2020/core/utils/digest/
├── DigestUtils.java               // 摘要算法工具类
└── MessageDigestAlgorithms.java   // 算法常量定义
```

## 注意事项

1. MD5 和 SHA-1 已被证明存在碰撞风险，安全敏感场景推荐使用 SHA-256 或更高
2. 密码存储不推荐直接使用简单摘要，应配合加盐或使用 bcrypt 等专用密码哈希算法（security 模块的 `BCryptPasswordEncoder`）
3. 大文件摘要时注意内存使用，文件方法使用流式读取不会一次性加载到内存
