package io.github.dengchen2020.core.utils.encrypt;

import javax.crypto.Cipher;
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

    private static final String ECB_PKCS1Padding = "RSA/ECB/PKCS1Padding";

    /**
     * 2048位RSA推荐的最大加密字节数(使用PKCS1Padding)
     */
    public static final int DEFAULT_MAX_ENCRYPT_BLOCK = 245;

    /**
     * 2048位RSA推荐的最大解密字节数
     */
    public static final int DEFAULT_MAX_DECRYPT_BLOCK = 256;

    /**
     * 生成 RSA 密钥对 2048位
     * @return 密钥对对象
     */
    public static KeyPair generateKeyPair() {
        return generateKeyPair(2048);
    }

    /**
     * 生成 RSA 密钥对
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
     * RSA 公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @return 加密后的 Base64 编码字符串
     */
    public static String encrypt(String data, PublicKey publicKey, String transformation) {
        try {
            return encrypt(data, publicKey, Cipher.getInstance(transformation));
        } catch (Exception e) {
            throw new RSAEncryptException(e);
        }
    }

    /**
     * RSA 公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param cipher 密码对象
     * @return 加密后的 Base64 编码字符串
     */
    public static String encrypt(String data, PublicKey publicKey, Cipher cipher) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RSAEncryptException(e);
        }
    }

    /**
     * RSA 公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @return 加密后的 Base64 编码字符串
     */
    public static String encrypt(String data, PublicKey publicKey) {
        return encrypt(data, publicKey, ECB_OAEPWithSHA256_MGF1Padding);
    }

    /**
     * RSA 公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param transformation 转换名称
     * @return 加密后的 Base64 编码字符串
     */
    public static String encrypt(byte[] data, PublicKey publicKey, String transformation) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(data);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RSAEncryptException(e);
        }
    }

    /**
     * RSA 公钥加密
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @return 加密后的 Base64 编码字符串
     */
    public static String encrypt(byte[] data, PublicKey publicKey) {
        return encrypt(data, publicKey, ECB_OAEPWithSHA256_MGF1Padding);
    }

    /**
     * 分段加密 - 处理长数据
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param transformation 转换名称
     * @param maxEncryptBlock 最大加密块大小
     * @return 加密后的 Base64 编码字符串
     */
    public static String encryptBySegment(byte[] data, PublicKey publicKey, String transformation, int maxEncryptBlock) {
        try {
            return encryptBySegment(data, publicKey, Cipher.getInstance(transformation), maxEncryptBlock);
        } catch (Exception e) {
            throw new RSAEncryptException(e);
        }
    }

    /**
     * 分段加密 - 处理长数据
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param maxEncryptBlock 最大加密块大小
     * @param cipher 密码对象
     * @return 加密后的 Base64 编码字符串
     */
    public static String encryptBySegment(byte[] data, PublicKey publicKey, Cipher cipher, int maxEncryptBlock) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            int inputLen = data.length;
            int offset = 0;
            byte[] resultBytes = new byte[0];
            byte[] cache;
            // 分段加密
            while (inputLen - offset > 0) {
                if (inputLen - offset > maxEncryptBlock) {
                    cache = cipher.doFinal(data, offset, maxEncryptBlock);
                    offset += maxEncryptBlock;
                } else {
                    cache = cipher.doFinal(data, offset, inputLen - offset);
                    offset = inputLen;
                }
                // 拼接结果
                resultBytes = concatByteArray(resultBytes, cache);
            }
            return Base64.getEncoder().encodeToString(resultBytes);
        } catch (Exception e) {
            throw new RSAEncryptException(e);
        }
    }

    /**
     * 分段加密 - 使用默认块大小处理长数据
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @param transformation 转换名称
     * @return 加密后的 Base64 编码字符串
     */
    public static String encryptBySegment(byte[] data, PublicKey publicKey, String transformation) {
        int maxEncryptBlock = switch (transformation) {
            case ECB_PKCS1Padding -> DEFAULT_MAX_ENCRYPT_BLOCK;
            case ECB_OAEPWithSHA256_MGF1Padding -> 190;
            case null, default -> DEFAULT_MAX_DECRYPT_BLOCK;
        };
        return encryptBySegment(data, publicKey, transformation, maxEncryptBlock);
    }

    /**
     * 分段加密 - 使用默认块大小和转换方式处理长数据
     * @param data 待加密的数据
     * @param publicKey 公钥
     * @return 加密后的 Base64 编码字符串
     */
    public static String encryptBySegment(byte[] data, PublicKey publicKey) {
        return encryptBySegment(data, publicKey, ECB_OAEPWithSHA256_MGF1Padding, DEFAULT_MAX_ENCRYPT_BLOCK);
    }

    /**
     * 分段加密字符串 - 处理长数据
     * @param data 待加密的字符串
     * @param publicKey 公钥
     * @param transformation 转换名称
     * @param maxEncryptBlock 最大加密块大小
     * @return 加密后的 Base64 编码字符串
     */
    public static String encryptBySegment(String data, PublicKey publicKey, String transformation, int maxEncryptBlock) {
        return encryptBySegment(data.getBytes(), publicKey, transformation, maxEncryptBlock);
    }

    /**
     * 分段加密字符串 - 使用默认块大小处理长数据
     * @param data 待加密的字符串
     * @param publicKey 公钥
     * @param transformation 转换名称
     * @return 加密后的 Base64 编码字符串
     */
    public static String encryptBySegment(String data, PublicKey publicKey, String transformation) {
        return encryptBySegment(data.getBytes(), publicKey, transformation, DEFAULT_MAX_ENCRYPT_BLOCK);
    }

    /**
     * 分段加密字符串 - 使用默认块大小和转换方式处理长数据
     * @param data 待加密的字符串
     * @param publicKey 公钥
     * @return 加密后的 Base64 编码字符串
     */
    public static String encryptBySegment(String data, PublicKey publicKey) {
        return encryptBySegment(data.getBytes(), publicKey, ECB_OAEPWithSHA256_MGF1Padding, DEFAULT_MAX_ENCRYPT_BLOCK);
    }

    /**
     * RSA 私钥解密
     * @param encryptedData 加密后的 Base64 编码字符串
     * @param privateKey 私钥
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
     * RSA 私钥解密
     * @param encryptedData 加密后的 Base64 编码字符串
     * @param privateKey 私钥
     * @param cipher 密码对象
     * @return 解密后的数据
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey, Cipher cipher) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new RSADecryptException(e);
        }
    }

    /**
     * RSA 私钥解密
     * @param encryptedData 加密后的 Base64 编码字符串
     * @param privateKey 私钥
     * @return 解密后的数据
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey) {
        return decrypt(encryptedData, privateKey, ECB_OAEPWithSHA256_MGF1Padding);
    }

    /**
     * 分段解密 - 处理长数据
     * @param encryptedData 加密后的 Base64 编码字符串
     * @param privateKey 私钥
     * @param transformation 转换名称
     * @param maxDecryptBlock 最大解密块大小
     * @return 解密后的数据
     */
    public static String decryptBySegment(String encryptedData, PrivateKey privateKey, String transformation, int maxDecryptBlock) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] data = Base64.getDecoder().decode(encryptedData);
            int inputLen = data.length;
            int offset = 0;
            byte[] resultBytes = new byte[0];
            byte[] cache;
            // 分段解密
            while (inputLen - offset > 0) {
                if (inputLen - offset > maxDecryptBlock) {
                    cache = cipher.doFinal(data, offset, maxDecryptBlock);
                    offset += maxDecryptBlock;
                } else {
                    cache = cipher.doFinal(data, offset, inputLen - offset);
                    offset = inputLen;
                }
                // 拼接结果
                resultBytes = concatByteArray(resultBytes, cache);
            }

            return new String(resultBytes);
        } catch (Exception e) {
            throw new RSADecryptException(e);
        }
    }

    /**
     * 分段解密 - 使用默认块大小处理长数据
     * @param encryptedData 加密后的 Base64 编码字符串
     * @param privateKey 私钥
     * @param transformation 转换名称
     * @return 解密后的数据
     */
    public static String decryptBySegment(String encryptedData, PrivateKey privateKey, String transformation) {
        int maxEncryptBlock = switch (transformation) {
            case ECB_PKCS1Padding -> DEFAULT_MAX_ENCRYPT_BLOCK;
            case ECB_OAEPWithSHA256_MGF1Padding -> 190;
            case null, default -> DEFAULT_MAX_DECRYPT_BLOCK;
        };
        return decryptBySegment(encryptedData, privateKey, transformation, maxEncryptBlock);
    }

    /**
     * 分段解密 - 使用默认块大小和转换方式处理长数据
     * @param encryptedData 加密后的 Base64 编码字符串
     * @param privateKey 私钥
     * @return 解密后的数据
     */
    public static String decryptBySegment(String encryptedData, PrivateKey privateKey) {
        return decryptBySegment(encryptedData, privateKey, ECB_OAEPWithSHA256_MGF1Padding, DEFAULT_MAX_DECRYPT_BLOCK);
    }

    /**
     * 将 Base64 编码的公钥字符串转换为 PublicKey 对象
     * @param publicKeyStr Base64 编码的公钥字符串
     * @return PublicKey 对象
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
     * 将 Base64 编码的私钥字符串转换为 PrivateKey 对象
     * @param privateKeyStr Base64 编码的私钥字符串
     * @return PrivateKey 对象
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

    /**
     * 字节数组拼接
     * @param first 第一个字节数组
     * @param second 第二个字节数组
     * @return 拼接后的字节数组
     */
    private static byte[] concatByteArray(byte[] first, byte[] second) {
        if (first == null || first.length == 0) return second;
        if (second == null || second.length == 0) return first;
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

}
