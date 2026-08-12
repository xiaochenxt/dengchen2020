package io.github.dengchen2020.core.utils.digest;

import io.github.dengchen2020.core.utils.StrUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 标准摘要算法工具类
 * @author xiaochen
 * @since 2024/12/26
 */
@NullMarked
public abstract class DigestUtils {

    /**
     * Package-private 用于测试。
     */
    static final int BUFFER_SIZE = 1024;

    /**
     * 读取字节数组并返回数据的摘要。提供与其他方法的对称性。
     *
     * @param messageDigest 要使用的 MessageDigest（例如 MD5）
     * @param data          要摘要的数据
     * @return 摘要
     */
    public static byte[] digest(final MessageDigest messageDigest, final byte[] data) {
        return messageDigest.digest(data);
    }

    /**
     * 读取 ByteBuffer 并返回数据的摘要
     *
     * @param messageDigest 要使用的 MessageDigest（例如 MD5）
     * @param data          要摘要的数据
     * @return 摘要
     */
    public static byte[] digest(final MessageDigest messageDigest, final ByteBuffer data) {
        messageDigest.update(data);
        return messageDigest.digest();
    }

    /**
     * 读取 File 并返回数据的摘要
     *
     * @param messageDigest 要使用的 MessageDigest（例如 MD5）
     * @param data          要摘要的数据
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] digest(final MessageDigest messageDigest, final File data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * 读取 InputStream 并返回数据的摘要
     *
     * @param messageDigest 要使用的 MessageDigest（例如 MD5）
     * @param data          要摘要的数据
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] digest(final MessageDigest messageDigest, final InputStream data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * 读取 File 并返回数据的摘要
     *
     * @param messageDigest 要使用的 MessageDigest（例如 MD5）
     * @param data          要摘要的数据
     * @param options       选项 如何打开文件
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] digest(final MessageDigest messageDigest, final Path data, final OpenOption... options) throws IOException {
        return updateDigest(messageDigest, data, options).digest();
    }

    /**
     * 使用非阻塞 io （NIO） 读取 RandomAccessFile 并返回数据的摘要
     *
     * @param messageDigest 要使用的 MessageDigest（例如 MD5）
     * @param data          要摘要的数据
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] digest(final MessageDigest messageDigest, final RandomAccessFile data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * 获取给定 {@code 算法} 的 {@code MessageDigest}。
     *
     * @param algorithm 请求的算法的名称。看
     *                  <a href="https://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA" >Java 中的附录 A
     * 加密体系结构参考指南</a> 了解有关标准算法名称的信息。
     * @return A digest instance.
     * @see MessageDigest#getInstance(String)
     * @throws IllegalArgumentException 当捕获 {@link NoSuchAlgorithmException} 时。
     */
    public static MessageDigest getDigest(final String algorithm) {
        try {
            return getMessageDigest(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 获取给定 {@code 算法} 的 {@code MessageDigest}，如果获取算法时出现问题，则获取默认值。
     *
     * @param algorithm            请求的算法的名称。看
     *                             <a href="https://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA" > Java 中的附录 A
     * 加密体系结构参考指南</a>，了解有关标准算法名称的信息。
     * @param defaultMessageDigest 默认的 MessageDigest.
     * @return 摘要实例。
     * @see MessageDigest#getInstance(String)
     * @throws IllegalArgumentException 当捕获 {@link NoSuchAlgorithmException} 时。
     */
    @Nullable
    public static MessageDigest getDigest(final String algorithm, final @Nullable MessageDigest defaultMessageDigest) {
        try {
            return getMessageDigest(algorithm);
        } catch (final Exception e) {
            return defaultMessageDigest;
        }
    }

    /**
     * 获取 MD2 MessageDigest。
     *
     * @return 一个 MD2摘要 实例。
     * @throws IllegalArgumentException 当捕获 {@link NoSuchAlgorithmException} 时，这不应该发生，因为 MD2 是一种内置算法
     * @see MessageDigestAlgorithms#MD2
     */
    public static MessageDigest getMd2Digest() {
        return getDigest(MessageDigestAlgorithms.MD2);
    }

    /**
     * 获取 MD5 MessageDigest。
     *
     * @return An MD5摘要 instance.
     * @throws IllegalArgumentException 当捕获 {@link NoSuchAlgorithmException} 时，这不应该发生，因为 MD5 是一种内置算法
     * @see MessageDigestAlgorithms#MD5
     */
    public static MessageDigest getMd5Digest() {
        return getDigest(MessageDigestAlgorithms.MD5);
    }

    /**
     * 获取给定 {@code 算法} 的 {@code MessageDigest}。
     *
     * @param algorithm 请求的算法的名称。看
     *                  <a href="https://docs.oracle.com/javase/6/docs/technotes/guides/security/crypto/CryptoSpec.html#AppA" > Java 中的附录 A
     * 加密体系结构参考指南</a> 了解有关标准算法名称的信息。
     * @return 摘要实例。
     * @see MessageDigest#getInstance(String)
     * @throws NoSuchAlgorithmException 如果没有 Provider 支持指定算法的 MessageDigestSpi 实现。
     */
    private static MessageDigest getMessageDigest(final String algorithm) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(algorithm);
    }

    /**
     * 获取 SHA-1 摘要。
     *
     * @return 一个 SHA-1 摘要实例。
     * @throws IllegalArgumentException 当捕获 {@link NoSuchAlgorithmException} 时，这不应该发生，因为 SHA-1 是一种内置算法
     * @see MessageDigestAlgorithms#SHA_1
     */
    public static MessageDigest getSha1Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_1);
    }

    /**
     * 获取 SHA-256 摘要。
     *
     * @return 一个 SHA-256 摘要实例。
     * @throws IllegalArgumentException 捕获 {@link NoSuchAlgorithmException} 时，这不应该发生，因为 SHA-256 是一种内置算法
     * @see MessageDigestAlgorithms#SHA_256
     */
    public static MessageDigest getSha256Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_256);
    }

    /**
     * 获取 SHA3-224 摘要。
     *
     * @return 一个 SHA3-224 摘要实例。
     * @throws IllegalArgumentException 捕获 {@link NoSuchAlgorithmException} 时，这不应该发生在 Oracle Java 9 及更高版本上。
     * @see MessageDigestAlgorithms#SHA3_224
     */
    public static MessageDigest getSha3_224Digest() {
        return getDigest(MessageDigestAlgorithms.SHA3_224);
    }

    /**
     * 返回 SHA3-256 摘要。
     *
     * @return 一个 SHA3-256 摘要实例。
     * @throws IllegalArgumentException 捕获 {@link NoSuchAlgorithmException} 时，这不应该发生在 Oracle Java 9 及更高版本上。
     * @see MessageDigestAlgorithms#SHA3_256
     */
    public static MessageDigest getSha3_256Digest() {
        return getDigest(MessageDigestAlgorithms.SHA3_256);
    }

    /**
     * 获取 SHA3-384 摘要。
     *
     * @return 一个 SHA3-384 摘要实例。
     * @throws IllegalArgumentException 捕获 {@link NoSuchAlgorithmException} 时，这不应该发生在 Oracle Java 9 及更高版本上。
     * @see MessageDigestAlgorithms#SHA3_384
     */
    public static MessageDigest getSha3_384Digest() {
        return getDigest(MessageDigestAlgorithms.SHA3_384);
    }

    /**
     * 获取 SHA3-512 摘要。
     *
     * @return 一个 SHA3-512 摘要实例。
     * @throws IllegalArgumentException 捕获 {@link NoSuchAlgorithmException} 时，这不应该发生在 Oracle Java 9 及更高版本上。
     * @see MessageDigestAlgorithms#SHA3_512
     */
    public static MessageDigest getSha3_512Digest() {
        return getDigest(MessageDigestAlgorithms.SHA3_512);
    }

    /**
     * 获取 SHA-384 摘要。
     *
     * @return 一个 SHA-384 摘要实例。
     * @throws IllegalArgumentException 当捕获 {@link NoSuchAlgorithmException} 时，这不应该发生，因为 SHA-384 是一种内置算法
     * @see MessageDigestAlgorithms#SHA_384
     */
    public static MessageDigest getSha384Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_384);
    }

    /**
     * 获取 SHA-512/224 摘要。
     *
     * @return 一个 SHA-512/224 摘要实例。
     * @throws IllegalArgumentException（捕获 {@link NoSuchAlgorithmException} 时）。
     * @see MessageDigestAlgorithms#SHA_512_224
     */
    public static MessageDigest getSha512_224Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_512_224);
    }

    /**
     * 获取 SHA-512/256 摘要。
     *
     * @return 一个 SHA-512/256 摘要实例。
     * @throws IllegalArgumentException（捕获 {@link NoSuchAlgorithmException} 时）。
     * @see MessageDigestAlgorithms#SHA_512_224
     */
    public static MessageDigest getSha512_256Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_512_256);
    }

    /**
     * 获取 SHA-512 摘要。
     *
     * @return 一个 SHA-512 摘要实例。
     * @throws IllegalArgumentException when a {@link NoSuchAlgorithmException} is caught, which should never happen because SHA-512 is a built-in algorithm
     * @see MessageDigestAlgorithms#SHA_512
     */
    public static MessageDigest getSha512Digest() {
        return getDigest(MessageDigestAlgorithms.SHA_512);
    }

    /**
     * 测试该算法是否受支持。
     *
     * @param messageDigestAlgorithm 算法名称
     * @return {@code true} 如果可以找到算法
     */
    public static boolean isAvailable(final String messageDigestAlgorithm) {
        return getDigest(messageDigestAlgorithm, null) != null;
    }

    /**
     * 计算MD2摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return MD2 摘要
     */
    public static byte[] md2(final byte[] data) {
        return getMd2Digest().digest(data);
    }

    /**
     * 计算MD2摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return MD2摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] md2(final InputStream data) throws IOException {
        return digest(getMd2Digest(), data);
    }

    /**
     * 计算MD2摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return MD2摘要
     */
    public static byte[] md2(final String data) {
        return md2(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算MD2摘要并返回32个字符的十六进制字符串的值。
     *
     * @param data 要摘要的数据
     * @return MD2摘要转换为十六进制字符串
     */
    public static String md2Hex(final byte[] data) {
        return StrUtils.formatHex(md2(data));
    }

    /**
     * 计算MD2摘要并返回32个字符的十六进制字符串的值。
     *
     * @param data 要摘要的数据
     * @return MD2摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String md2Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(md2(data));
    }

    /**
     * 计算MD2摘要并返回32个字符的十六进制字符串的值。
     *
     * @param data 要摘要的数据
     * @return MD2摘要转换为十六进制字符串
     */
    public static String md2Hex(final String data) {
        return StrUtils.formatHex(md2(data));
    }

    /**
     * 计算MD5摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return MD5摘要
     */
    public static byte[] md5(final byte[] data) {
        return getMd5Digest().digest(data);
    }

    /**
     * 计算MD5摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return MD5摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] md5(final InputStream data) throws IOException {
        return digest(getMd5Digest(), data);
    }

    /**
     * 计算MD5摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为bytes
     * @return MD5摘要
     */
    public static byte[] md5(final String data) {
        return md5(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算MD5摘要并返回32个字符的十六进制字符串的值。
     *
     * @param data 要摘要的数据
     * @return MD5摘要转换为十六进制字符串
     */
    public static String md5Hex(final byte[] data) {
        return StrUtils.formatHex(md5(data));
    }

    /**
     * 计算MD5摘要并返回32个字符的十六进制字符串的值。
     *
     * @param data 要摘要的数据
     * @return MD5摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String md5Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(md5(data));
    }

    /**
     * 计算MD5摘要并返回32个字符的十六进制字符串的值。
     *
     * @param data 要摘要的数据
     * @return MD5摘要转换为十六进制字符串
     */
    public static String md5Hex(final String data) {
        return StrUtils.formatHex(md5(data));
    }

    /**
     * 计算SHA-1摘要并将值作为 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-1摘要
     */
    public static byte[] sha1(final byte[] data) {
        return getSha1Digest().digest(data);
    }

    /**
     * 计算SHA-1摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-1摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha1(final InputStream data) throws IOException {
        return digest(getSha1Digest(), data);
    }

    /**
     * 计算 SHA-1 摘要并将返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA-1 摘要
     */
    public static byte[] sha1(final String data) {
        return sha1(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SHA-1 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-1 摘要转换为十六进制字符串
     */
    public static String sha1Hex(final byte[] data) {
        return StrUtils.formatHex(sha1(data));
    }

    /**
     * 计算 SHA-1 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-1 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha1Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha1(data));
    }

    /**
     * 计算 SHA-1 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-1 摘要转换为十六进制字符串
     */
    public static String sha1Hex(final String data) {
        return StrUtils.formatHex(sha1(data));
    }

    /**
     * 计算 SHA-256 摘要并将返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-256 摘要
     */
    public static byte[] sha256(final byte[] data) {
        return getSha256Digest().digest(data);
    }

    /**
     * 计算 SHA-256 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-256 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha256(final InputStream data) throws IOException {
        return digest(getSha256Digest(), data);
    }

    /**
     * 计算SHA-256摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA-256 摘要
     */
    public static byte[] sha256(final String data) {
        return sha256(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算SHA-256摘要并返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-256 摘要转换为十六进制字符串
     */
    public static String sha256Hex(final byte[] data) {
        return StrUtils.formatHex(sha256(data));
    }

    /**
     * 计算 SHA-256 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-256 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha256Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha256(data));
    }

    /**
     * 计算 SHA-256 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-256 摘要转换为十六进制字符串
     */
    public static String sha256Hex(final String data) {
        return StrUtils.formatHex(sha256(data));
    }

    /**
     * 计算 SHA3-224 摘要并将返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA3-224 摘要
     */
    public static byte[] sha3_224(final byte[] data) {
        return getSha3_224Digest().digest(data);
    }

    /**
     * 计算 SHA3-224 摘要并将返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA3-224 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha3_224(final InputStream data) throws IOException {
        return digest(getSha3_224Digest(), data);
    }

    /**
     * 计算 SHA3-224 摘要并将返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA3-224 摘要
     */
    public static byte[] sha3_224(final String data) {
        return sha3_224(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SHA3-224 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-224 摘要转换为十六进制字符串
     */
    public static String sha3_224Hex(final byte[] data) {
        return StrUtils.formatHex(sha3_224(data));
    }

    /**
     * 计算 SHA3-224 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-224 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha3_224Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha3_224(data));
    }

    /**
     * 计算 SHA3-224 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-224 摘要转换为十六进制字符串
     */
    public static String sha3_224Hex(final String data) {
        return StrUtils.formatHex(sha3_224(data));
    }

    /**
     * 计算 SHA3-256 摘要并将返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA3-256 digest
     */
    public static byte[] sha3_256(final byte[] data) {
        return getSha3_256Digest().digest(data);
    }

    /**
     * 计算 SHA3-256 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA3-256 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha3_256(final InputStream data) throws IOException {
        return digest(getSha3_256Digest(), data);
    }

    /**
     * 计算 SHA3-256 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA3-256 摘要
     */
    public static byte[] sha3_256(final String data) {
        return sha3_256(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SHA3-256 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-256 摘要转换为十六进制字符串
     */
    public static String sha3_256Hex(final byte[] data) {
        return StrUtils.formatHex(sha3_256(data));
    }

    /**
     * 计算 SHA3-256 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-256 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha3_256Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha3_256(data));
    }

    /**
     * 计算 SHA3-256 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-256 摘要转换为十六进制字符串
     */
    public static String sha3_256Hex(final String data) {
        return StrUtils.formatHex(sha3_256(data));
    }

    /**
     * 计算 SHA3-384 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA3-384 digest
     */
    public static byte[] sha3_384(final byte[] data) {
        return getSha3_384Digest().digest(data);
    }

    /**
     * 计算 SHA3-384 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA3-384 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha3_384(final InputStream data) throws IOException {
        return digest(getSha3_384Digest(), data);
    }

    /**
     * 计算 SHA3-384 摘要返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA3-384 摘要
     */
    public static byte[] sha3_384(final String data) {
        return sha3_384(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SHA3-384 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-384 摘要转换为十六进制字符串
     */
    public static String sha3_384Hex(final byte[] data) {
        return StrUtils.formatHex(sha3_384(data));
    }

    /**
     * 计算 SHA3-384 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-384 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha3_384Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha3_384(data));
    }

    /**
     * 计算 SHA3-384 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-384 摘要转换为十六进制字符串
     */
    public static String sha3_384Hex(final String data) {
        return StrUtils.formatHex(sha3_384(data));
    }

    /**
     * 计算 SHA3-512 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA3-512 摘要
     */
    public static byte[] sha3_512(final byte[] data) {
        return getSha3_512Digest().digest(data);
    }

    /**
     * 计算 SHA3-512 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA3-512 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha3_512(final InputStream data) throws IOException {
        return digest(getSha3_512Digest(), data);
    }

    /**
     * 计算 SHA3-512 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA3-512 摘要
     */
    public static byte[] sha3_512(final String data) {
        return sha3_512(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SHA3-512 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-512 摘要转换为十六进制字符串
     */
    public static String sha3_512Hex(final byte[] data) {
        return StrUtils.formatHex(sha3_512(data));
    }

    /**
     * 计算 SHA3-512 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-512 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha3_512Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha3_512(data));
    }

    /**
     * 计算 SHA3-512 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA3-512 摘要转换为十六进制字符串
     */
    public static String sha3_512Hex(final String data) {
        return StrUtils.formatHex(sha3_512(data));
    }

    /**
     * 计算 SHA-384 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-384 摘要
     */
    public static byte[] sha384(final byte[] data) {
        return getSha384Digest().digest(data);
    }

    /**
     * 计算 SHA-384 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-384 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha384(final InputStream data) throws IOException {
        return digest(getSha384Digest(), data);
    }

    /**
     * 计算 SHA-384 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA-384 摘要
     */
    public static byte[] sha384(final String data) {
        return sha384(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SHA-384 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-384 摘要转换为十六进制字符串
     */
    public static String sha384Hex(final byte[] data) {
        return StrUtils.formatHex(sha384(data));
    }

    /**
     * 计算 SHA-384 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-384 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha384Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha384(data));
    }

    /**
     * 计算 SHA-384 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-384 摘要转换为十六进制字符串
     */
    public static String sha384Hex(final String data) {
        return StrUtils.formatHex(sha384(data));
    }

    /**
     * 计算 SHA-512 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-512 摘要
     */
    public static byte[] sha512(final byte[] data) {
        return getSha512Digest().digest(data);
    }

    /**
     * 计算 SHA-512 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-512 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha512(final InputStream data) throws IOException {
        return digest(getSha512Digest(), data);
    }

    /**
     * 计算 SHA-512 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA-512 摘要
     */
    public static byte[] sha512(final String data) {
        return sha512(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SHA-512/224 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-512/224 摘要
     */
    public static byte[] sha512_224(final byte[] data) {
        return getSha512_224Digest().digest(data);
    }

    /**
     * 计算 SHA-512/224 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-512/224 摘要
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha512_224(final InputStream data) throws IOException {
        return digest(getSha512_224Digest(), data);
    }

    /**
     * 计算 SHA-512/224 摘要并返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA-512/224 摘要
     */
    public static byte[] sha512_224(final String data) {
        return sha512_224(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SHA-512/224 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-512/224 摘要转换为十六进制字符串
     */
    public static String sha512_224Hex(final byte[] data) {
        return StrUtils.formatHex(sha512_224(data));
    }

    /**
     * 计算 SHA-512/224 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-512/224 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha512_224Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha512_224(data));
    }

    /**
     * 计算 SHA-512/224 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-512/224 摘要转换为十六进制字符串
     */
    public static String sha512_224Hex(final String data) {
        return StrUtils.formatHex(sha512_224(data));
    }

    /**
     * 计算 SHA-512/256 摘要并将返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-512/256 摘要
     */
    public static byte[] sha512_256(final byte[] data) {
        return getSha512_256Digest().digest(data);
    }

    /**
     * 计算 SHA-512/256 摘要并将返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据
     * @return SHA-512/256 digest
     * @throws IOException 从流中读取时出错
     */
    public static byte[] sha512_256(final InputStream data) throws IOException {
        return digest(getSha512_256Digest(), data);
    }

    /**
     * 计算 SHA-512/256 摘要并将返回值 {@code byte[]}.
     *
     * @param data 要摘要的数据; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return SHA-512/224 摘要
     */
    public static byte[] sha512_256(final String data) {
        return sha512_256(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SHA-512/256 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-512/256 摘要转换为十六进制字符串
     */
    public static String sha512_256Hex(final byte[] data) {
        return StrUtils.formatHex(sha512_256(data));
    }

    /**
     * 计算 SHA-512/256 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-512/256 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha512_256Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha512_256(data));
    }

    /**
     * 计算 SHA-512/256 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-512/256 摘要转换为十六进制字符串
     */
    public static String sha512_256Hex(final String data) {
        return StrUtils.formatHex(sha512_256(data));
    }

    /**
     * 计算 SHA-512 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-512 摘要转换为十六进制字符串
     */
    public static String sha512Hex(final byte[] data) {
        return StrUtils.formatHex(sha512(data));
    }

    /**
     * 计算 SHA-512 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-512 摘要转换为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public static String sha512Hex(final InputStream data) throws IOException {
        return StrUtils.formatHex(sha512(data));
    }

    /**
     * 计算 SHA-512 摘要并将返回值转换为十六进制字符串.
     *
     * @param data 要摘要的数据
     * @return SHA-512 摘要转换为十六进制字符串
     */
    public static String sha512Hex(final String data) {
        return StrUtils.formatHex(sha512(data));
    }

    /**
     * 更新给定的 {@link MessageDigest}.
     *
     * @param messageDigest 要更新的 {@link MessageDigest}
     * @param valueToDigest 用于更新 {@link MessageDigest} 的值
     * @return 更新后的 {@link MessageDigest}
     */
    public static MessageDigest updateDigest(final MessageDigest messageDigest, final byte[] valueToDigest) {
        messageDigest.update(valueToDigest);
        return messageDigest;
    }

    /**
     * 更新给定的 {@link MessageDigest}。
     *
     * @param messageDigest 要更新的 {@link MessageDigest}
     * @param valueToDigest 用于更新 {@link MessageDigest} 的值
     * @return 更新后的 {@link MessageDigest}
     */
    public static MessageDigest updateDigest(final MessageDigest messageDigest, final ByteBuffer valueToDigest) {
        messageDigest.update(valueToDigest);
        return messageDigest;
    }

    /**
     * 读取 File 并更新数据的摘要
     *
     * @param digest 要使用的 MessageDigest（例如 MD5）
     * @param data   要摘要的数据
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public static MessageDigest updateDigest(final MessageDigest digest, final File data) throws IOException {
        try (final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(data))) {
            return updateDigest(digest, inputStream);
        }
    }

    /**
     * 读取 RandomAccessFile 并使用非阻塞 io （NIO） 更新数据的摘要。
     * TODO 考虑这是否应该公开。
     *
     * @param digest 要使用的 MessageDigest（例如 MD5）
     * @param data   要摘要的数据
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    private static MessageDigest updateDigest(final MessageDigest digest, final FileChannel data) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        while (data.read(buffer) > 0) {
            buffer.flip();
            digest.update(buffer);
            buffer.clear();
        }
        return digest;
    }

    /**
     * 读取 InputStream 并更新数据的摘要
     *
     * @param digest      要使用的 MessageDigest（例如 MD5）
     * @param inputStream 要摘要的数据
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public static MessageDigest updateDigest(final MessageDigest digest, final InputStream inputStream) throws IOException {
        final byte[] buffer = new byte[BUFFER_SIZE];
        int read = inputStream.read(buffer, 0, BUFFER_SIZE);

        while (read > -1) {
            digest.update(buffer, 0, read);
            read = inputStream.read(buffer, 0, BUFFER_SIZE);
        }

        return digest;
    }

    /**
     * 读取 Path 并更新数据的摘要
     *
     * @param digest  要使用的 MessageDigest（例如 MD5）
     * @param path    要摘要的数据
     * @param options 选项 如何打开文件
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public static MessageDigest updateDigest(final MessageDigest digest, final Path path, final OpenOption... options) throws IOException {
        try (final BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(path, options))) {
            return updateDigest(digest, inputStream);
        }
    }

    /**
     * 读取 RandomAccessFile 并使用非阻塞 io （NIO） 更新数据的摘要
     *
     * @param digest 要使用的 MessageDigest（例如 MD5）
     * @param data   要摘要的数据
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    // 关闭 RandomAccessFile 将关闭通道。
    public static MessageDigest updateDigest(final MessageDigest digest, final RandomAccessFile data) throws IOException {
        return updateDigest(digest, data.getChannel());
    }

    /**
     * 从 String 更新给定的 {@link MessageDigest} （使用 UTF-8 转换为字节）。
     * <p>
     * 要使用其他字符集更新摘要进行转换，请使用 String 将 String 转换为字节数组
     * {@link String#getBytes(java.nio.charset.Charset)} 并将其传递给 {@link DigestUtils#updateDigest(MessageDigest, byte[])} 方法
     *
     * @param messageDigest 要更新的 {@link MessageDigest}
     * @param valueToDigest 更新 {@link MessageDigest} 的值; 使用 {@link StandardCharsets#UTF_8} 转换为 bytes
     * @return 更新后的 {@link MessageDigest}
     */
    public static MessageDigest updateDigest(final MessageDigest messageDigest, final String valueToDigest) {
        messageDigest.update(valueToDigest.getBytes(StandardCharsets.UTF_8));
        return messageDigest;
    }

    private final MessageDigest messageDigest;

    /**
     * 使用提供的 {@link MessageDigest} 参数
     * 然后，这可用于使用诸如 {@link #digest(byte[])} 和 {@link #digestAsHex(File)}.
     *
     * @param digest 使用 {@link MessageDigest}
     */
    public DigestUtils(final MessageDigest digest) {
        this.messageDigest = digest;
    }

    /**
     * 使用提供的 {@link MessageDigest} 参数
     * 然后，这可用于使用诸如 {@link #digest(byte[])} 和 {@link #digestAsHex(File)}.
     *
     * @param name 的名称使用 {@link MessageDigest}
     * @see #getDigest(String)
     * @throws IllegalArgumentException 当 {@link NoSuchAlgorithmException} 被捕获。
     */
    public DigestUtils(final String name) {
        this(getDigest(name));
    }

    /**
     * 读取字节数组并返回数据的摘要。
     *
     * @param data 要摘要的数据
     * @return 摘要
     */
    public byte[] digest(final byte[] data) {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * 读取 ByteBuffer 并返回数据的摘要
     *
     * @param data 要摘要的数据
     * @return 摘要
     *
     */
    public byte[] digest(final ByteBuffer data) {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * 读取 File 并返回数据的摘要
     *
     * @param data 要摘要的数据
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public byte[] digest(final File data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * 读取 InputStream 并返回数据的摘要
     *
     * @param data 要摘要的数据
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public byte[] digest(final InputStream data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * 读取 File 并返回数据的摘要
     *
     * @param data    要摘要的数据
     * @param options 选项 如何打开文件
     * @return 摘要
     * @throws IOException 从流中读取时出错
     */
    public byte[] digest(final Path data, final OpenOption... options) throws IOException {
        return updateDigest(messageDigest, data, options).digest();
    }

    /**
     * 读取字节数组并返回数据的摘要。
     *
     * @param data 要摘要的数据被视为 UTF-8 字符串
     * @return 摘要
     */
    public byte[] digest(final String data) {
        return updateDigest(messageDigest, data).digest();
    }

    /**
     * 读取字节数组并返回数据的摘要。
     *
     * @param data 要摘要的数据
     * @return 摘要为十六进制字符串
     */
    public String digestAsHex(final byte[] data) {
        return StrUtils.formatHex(digest(data));
    }

    /**
     * 读取 ByteBuffer 并返回数据的摘要
     *
     * @param data 要消化的数据
     * @return 摘要为十六进制字符串
     *
     */
    public String digestAsHex(final ByteBuffer data) {
        return StrUtils.formatHex(digest(data));
    }

    /**
     * 读取 File 并返回数据的摘要
     *
     * @param data 要摘要的数据
     * @return 摘要为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public String digestAsHex(final File data) throws IOException {
        return StrUtils.formatHex(digest(data));
    }

    /**
     * 读取 InputStream 并返回数据的摘要
     *
     * @param data 要摘要的数据
     * @return 摘要为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public String digestAsHex(final InputStream data) throws IOException {
        return StrUtils.formatHex(digest(data));
    }

    /**
     * 读取 File 并返回数据的摘要
     *
     * @param data 要消化的数据
     * @param options 如何打开文件
     * @return 摘要为十六进制字符串
     * @throws IOException 从流中读取时出错
     */
    public String digestAsHex(final Path data, final OpenOption... options) throws IOException {
        return StrUtils.formatHex(digest(data, options));
    }

    /**
     * 读取字节数组并返回数据的摘要。
     *
     * @param data 要摘要的数据被视为 UTF-8 字符串
     * @return 摘要为十六进制字符串
     */
    public String digestAsHex(final String data) {
        return StrUtils.formatHex(digest(data));
    }

    /**
     * 返回消息摘要实例。
     *
     * @return 消息摘要实例
     */
    public MessageDigest getMessageDigest() {
        return messageDigest;
    }

}
