package io.github.dengchen2020.core.utils.encrypt;

import org.jspecify.annotations.NullMarked;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES加解密工具类
 * <pre>对接第三方时第三方可能有特殊处理，优先使用对方提供的代码</pre>
 * <p>jdk并不支持所有的加解密算法，并且高版本jdk可能移除一些不安全或使用率低的算法，可引入{@code bcprov-jdk18on}依赖获得相对完整的支持</p>
 * <pre>
 * {@code <dependency>
 *     <groupId>org.bouncycastle</groupId>
 *     <artifactId>bcprov-jdk18on</artifactId>
 *     <version>1.81</version>
 * </dependency>
 * }
 * </pre>
 * <p>引入依赖后，全局调用一次注册即可<pre>
 * {@code
 *   // 注册 Bouncy Castle 提供者
 *   Security.addProvider(new BouncyCastleProvider());
 * }
 * </pre></p>
 * @author xiaochen
 * @since 2025/3/12
 */
@NullMarked
public abstract class AESUtils {

    public static final String ALGORITHM = "AES";

    public static final String CBC_NOPADDING = "AES/CBC/NoPadding";

    public static final String CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";

    public static final String GCM_NOPADDING = "AES/GCM/NoPadding";

    /**
     * 生成 AES 密钥
     * @param keySize 密钥长度，可选 128、192、256
     * @return 密钥的 Base64 编码字符串
     * @throws Exception 异常
     */
    public static String generateKey(int keySize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(keySize);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new AESGenerateKeyException(e);
        }
    }

    /**
     * 生成初始化向量（IV）
     * @return IV 的 Base64 编码字符串
     */
    public static String generateIV() {
        byte[] iv = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return Base64.getEncoder().encodeToString(iv);
    }

    /**
     * AES 加密
     * @param plainText 明文
     * @param keyBase64 密钥的 Base64 编码字符串
     * @param iv 初始化向量
     * @return 密文的 Base64 编码字符串
     */
    public static String encrypt(String plainText, String keyBase64, IvParameterSpec iv, String transformation) {
        try {
            return encrypt(plainText, keyBase64, iv, Cipher.getInstance(transformation));
        } catch (Exception e) {
            throw new AESEncryptException(e);
        }
    }

    /**
     * AES 加密
     * @param plainText 明文
     * @param keyBase64 密钥的 Base64 编码字符串
     * @param iv 初始化向量
     * @param cipher 密码对象
     * @return 密文的 Base64 编码字符串
     */
    public static String encrypt(String plainText, String keyBase64, IvParameterSpec iv, Cipher cipher) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new AESEncryptException(e);
        }
    }

    /**
     * AES 加密
     * @param plainText 明文
     * @param keyBase64 密钥的 Base64 编码字符串
     * @param ivBase64 初始化向量的 Base64 编码字符串
     * @return 密文的 Base64 编码字符串
     */
    public static String encrypt(String plainText, String keyBase64, String ivBase64, String transformation) {
        return encrypt(plainText, keyBase64, new IvParameterSpec(Base64.getDecoder().decode(ivBase64)), transformation);
    }

    /**
     * AES CBC PKCS5Padding 加密
     * @param plainText 明文
     * @param keyBase64 密钥的 Base64 编码字符串
     * @param ivBase64 初始化向量的 Base64 编码字符串
     * @return 密文的 Base64 编码字符串
     */
    public static String encrypt(String plainText, String keyBase64, String ivBase64) {
        return encrypt(plainText, keyBase64, ivBase64, CBC_PKCS5_PADDING);
    }

    /**
     * AES 解密
     * @param cipherText 密文的 Base64 编码字符串
     * @param keyBase64 密钥的 Base64 编码字符串
     * @param iv 初始化向量
     * @return 明文
     */
    public static String decrypt(String cipherText, String keyBase64, IvParameterSpec iv, String transformation) {
        try {
            return decrypt(cipherText, keyBase64, iv, Cipher.getInstance(transformation));
        } catch (Exception e) {
            throw new AESDecryptException(e);
        }
    }

    /**
     * AES 解密
     * @param cipherText 密文的 Base64 编码字符串
     * @param keyBase64 密钥的 Base64 编码字符串
     * @param iv 初始化向量
     * @param cipher 密码对象
     * @return 明文
     */
    public static String decrypt(String cipherText, String keyBase64, IvParameterSpec iv, Cipher cipher) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
            byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, iv);
            byte[] decryptedBytes = cipher.doFinal(cipherBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new AESDecryptException(e);
        }
    }

    /**
     * AES 解密
     * @param cipherText 密文的 Base64 编码字符串
     * @param keyBase64 密钥的 Base64 编码字符串
     * @param ivBase64 初始化向量的 Base64 编码字符串
     * @return 明文
     */
    public static String decrypt(String cipherText, String keyBase64, String ivBase64, String transformation) {
        return decrypt(cipherText, keyBase64, new IvParameterSpec(Base64.getDecoder().decode(ivBase64)), transformation);
    }

    /**
     * AES CBC PKCS5Padding 解密
     * @param cipherText 密文的 Base64 编码字符串
     * @param keyBase64 密钥的 Base64 编码字符串
     * @param ivBase64 初始化向量的 Base64 编码字符串
     * @return 明文
     */
    public static String decrypt(String cipherText, String keyBase64, String ivBase64) {
        return decrypt(cipherText, keyBase64, ivBase64, CBC_PKCS5_PADDING);
    }

}
