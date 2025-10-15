package io.github.dengchen2020.core.utils.sign;

import org.jspecify.annotations.NonNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Hmac签名验签工具类
 * @author xiaochen
 * @since 2025/3/12
 */
public abstract class HMACUtils {

    public static final String HMAC_SHA256 = "HmacSHA256";

    public static final String HMAC_SHA384 = "HmacSHA384";

    public static final String HMAC_SHA512 = "HmacSHA512";

    public static byte[] sign(byte[] data, byte[] secret, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret, algorithm));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new HMACSignException(e);
        }
    }

    public static byte[] sign(String message, String secret, String algorithm) {
        return sign(message.getBytes(), secret.getBytes(), algorithm);
    }

    private static String encodeToString(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static byte[] decodeFromString(String data) {
        return Base64.getUrlDecoder().decode(data);
    }

    public static byte[] sha256(@NonNull String message, String secret) {
        return sign(message, secret, HMAC_SHA256);
    }

    public static String sha256ToString(@NonNull String message, String secret) {
        return encodeToString(sha256(message, secret));
    }

    public static byte[] sha384(@NonNull String message, String secret) {
        return sign(message, secret, HMAC_SHA384);
    }

    public static String sha384ToString(@NonNull String message, String secret) {
        return encodeToString(sha384(message, secret));
    }

    public static byte[] sha512(@NonNull String message, String secret) {
        return sign(message, secret, HMAC_SHA512);
    }

    public static String sha512ToString(@NonNull String message, String secret) {
        return encodeToString(sha512(message, secret));
    }

    public static boolean verifySha256(String message, String sign, String secret) {
        return verify(message, sign, secret, HMAC_SHA256);
    }

    public static boolean verifySha384(String message, String sign, String secret) {
        return verify(message, sign, secret, HMAC_SHA384);
    }

    public static boolean verifySha512(String message, String sign, String secret) {
        return verify(message, sign, secret, HMAC_SHA512);
    }

    public static boolean verify(byte[] data, byte[] signature, byte[] secret, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret, algorithm));
            byte[] actualSignature = mac.doFinal(data);
            return MessageDigest.isEqual(actualSignature, signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new HMACVerifyException(e);
        }
    }

    public static boolean verify(String message, String signature, String secret, String algorithm) {
        return verify(message.getBytes(), decodeFromString(signature), secret.getBytes(), algorithm);
    }

}
