package io.github.dengchen2020.core.utils.encrypt;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加解密工具类
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
public abstract class RSAUtils {

    public static final String ALGORITHM = "RSA";

    public static final String ECB_OAEPWithSHA256_MGF1Padding = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    /**
     * 生成RSA密钥对2048位
     * @return 密钥对对象
     */
    public static KeyPair generateKeyPair() {
        return generateKeyPair(2048);
    }

    /**
     * 生成RSA密钥对
     * @return 密钥对对象
     */
    public static KeyPair generateKeyPair(int keysize) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(keysize);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new RSAGenerateKeyException(e);
        }
    }

    /**
     * RSA公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @return 加密后的Base64编码字符串
     */
    public static String encrypt(String data, PublicKey publicKey, String transformation) {
        try {
            return encrypt(data, publicKey, Cipher.getInstance(transformation));
        } catch (Exception e) {
            throw new RSAEncryptException(e);
        }
    }

    /**
     * RSA公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param cipher 密码对象
     * @return 加密后的Base64编码字符串
     */
    public static String encrypt(String data, PublicKey publicKey, Cipher cipher) {
        return encrypt(data.getBytes(StandardCharsets.UTF_8), publicKey, cipher);
    }

    /**
     * RSA公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @return 加密后的Base64编码字符串
     */
    public static String encrypt(String data, PublicKey publicKey) {
        return encrypt(data, publicKey, ECB_OAEPWithSHA256_MGF1Padding);
    }

    /**
     * RSA公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param transformation 转换名称
     * @return 加密后的Base64编码字符串
     */
    public static String encrypt(byte[] data, PublicKey publicKey, String transformation) {
        try {
            return encrypt(data, publicKey, Cipher.getInstance(transformation));
        } catch (Exception e) {
            throw new RSAEncryptException(e);
        }
    }

    /**
     * RSA公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param cipher 密码对象
     * @return 加密后的Base64编码字符串
     */
    public static String encrypt(byte[] data, PublicKey publicKey, Cipher cipher) {
        return Base64.getEncoder().encodeToString(encryptToBytes(data, publicKey, cipher));
    }

    /**
     * RSA公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @return 加密后的Base64编码字符串
     */
    public static String encrypt(byte[] data, PublicKey publicKey) {
        return encrypt(data, publicKey, ECB_OAEPWithSHA256_MGF1Padding);
    }

    /**
     * RSA公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param cipher 密码对象
     * @return 加密后的原始字节数组
     */
    public static byte[] encryptToBytes(byte[] data, PublicKey publicKey, Cipher cipher) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RSAEncryptException(e);
        }
    }

    /**
     * RSA公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param transformation 转换名称
     * @return 加密后的原始字节数组
     */
    public static byte[] encryptToBytes(byte[] data, PublicKey publicKey, String transformation) {
        try {
            return encryptToBytes(data, publicKey, Cipher.getInstance(transformation));
        } catch (Exception e) {
            throw new RSAEncryptException(e);
        }
    }

    /**
     * RSA公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @return 加密后的原始字节数组
     */
    public static byte[] encryptToBytes(byte[] data, PublicKey publicKey) {
        return encryptToBytes(data, publicKey, ECB_OAEPWithSHA256_MGF1Padding);
    }

    /**
     * RSA私钥解密
     * @param encryptedData 加密后的Base64编码字符串
     * @param privateKey 私钥
     * @param transformation 转换名称
     * @return 解密后的数据
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey, String transformation) {
        try {
            return decrypt(encryptedData, privateKey, Cipher.getInstance(transformation));
        } catch (Exception e) {
            throw new RSADecryptException(e);
        }
    }

    /**
     * RSA私钥解密
     * @param encryptedData 加密后的Base64编码字符串
     * @param privateKey 私钥
     * @param cipher 密码对象
     * @return 解密后的数据
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey, Cipher cipher) {
        return new String(decryptToBytes(encryptedData, privateKey, cipher), StandardCharsets.UTF_8);
    }

    /**
     * RSA私钥解密
     * @param encryptedData 加密后的Base64编码字符串
     * @param privateKey 私钥
     * @return 解密后的数据
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey) {
        return decrypt(encryptedData, privateKey, ECB_OAEPWithSHA256_MGF1Padding);
    }

    /**
     * RSA私钥解密
     * @param encryptedData 加密后的Base64编码字符串
     * @param privateKey 私钥
     * @param cipher 密码对象
     * @return 解密后的原始字节数组
     */
    public static byte[] decryptToBytes(String encryptedData, PrivateKey privateKey, Cipher cipher) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        } catch (Exception e) {
            throw new RSADecryptException(e);
        }
    }

    /**
     * RSA私钥解密
     * @param encryptedData 加密后的Base64编码字符串
     * @param privateKey 私钥
     * @param transformation 转换名称
     * @return 解密后的原始字节数组
     */
    public static byte[] decryptToBytes(String encryptedData, PrivateKey privateKey, String transformation) {
        try {
            return decryptToBytes(encryptedData, privateKey, Cipher.getInstance(transformation));
        } catch (Exception e) {
            throw new RSADecryptException(e);
        }
    }

    /**
     * RSA私钥解密
     * @param encryptedData 加密后的Base64编码字符串
     * @param privateKey 私钥
     * @return 解密后的原始字节数组
     */
    public static byte[] decryptToBytes(String encryptedData, PrivateKey privateKey) {
        return decryptToBytes(encryptedData, privateKey, ECB_OAEPWithSHA256_MGF1Padding);
    }

    /**
     * 将 Base64 编码的公钥字符串转换为 {@link PublicKey} 对象
     * @param publicKeyStr Base64编码的公钥字符串
     * @return {@link PublicKey}
     */
    public static PublicKey getPublicKey(String publicKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new RSAGeneratePublicException(e);
        }
    }

    /**
     * 将 Base64 编码的私钥字符串转换为 {@link PrivateKey} 对象
     * @param privateKeyStr Base64编码的私钥字符串
     * @return {@link PrivateKey}
     */
    public static PrivateKey getPrivateKey(String privateKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RSAGeneratePrivateException(e);
        }
    }

}
