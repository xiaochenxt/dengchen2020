package io.github.dengchen2020.core.utils.digest;

import java.security.MessageDigest;

/**
 * 标准 {@link MessageDigest} <cite>Java 加密体系结构标准算法名称中的算法名称文档</cite>.
 * <p>此类是不可变的，并且是线程安全的。</p>
 * <ul>
 * <li>Java 8 及更高版本：SHA-224。</li>
 * <li>Java 9 及更高版本: SHA3-224, SHA3-256, SHA3-384, SHA3-512.</li>
 * </ul>
 *
 * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest">
 *      Java 8 Cryptography Architecture Standard Algorithm Name Documentation</a>
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html#messagedigest-algorithms">
 *      Java 11 Cryptography Architecture Standard Algorithm Name Documentation</a>
 * @see <a href="https://docs.oracle.com/en/java/javase/17/docs/specs/security/standard-names.html#messagedigest-algorithms">
 *      Java 17 Cryptography Architecture Standard Algorithm Name Documentation</a>
 * @see <a href="https://docs.oracle.com/en/java/javase/21/docs/specs/security/standard-names.html#messagedigest-algorithms">
 *      Java 21 Cryptography Architecture Standard Algorithm Name Documentation</a>
 *
 * @see <a href="https://dx.doi.org/10.6028/NIST.FIPS.180-4">FIPS PUB 180-4</a>
 * @see <a href="https://dx.doi.org/10.6028/NIST.FIPS.202">FIPS PUB 202</a>
 */
public abstract class MessageDigestAlgorithms {

    /**
     * RFC 1319 中定义的 MD2 消息摘要算法。
     */
    public static final String MD2 = "MD2";

    /**
     * RFC 1321 中定义的 MD5 消息摘要算法。
     */
    public static final String MD5 = "MD5";

    /**
     * FIPS PUB 180-2 中定义的 SHA-1 哈希算法。
     */
    public static final String SHA_1 = "SHA-1";

    /**
     * FIPS PUB 180-3 中定义的 SHA-224 哈希算法。
     * <p>存在于 Oracle Java 8 中。</p>
     */
    public static final String SHA_224 = "SHA-224";

    /**
     * FIPS PUB 180-2 中定义的 SHA-256 哈希算法。
     */
    public static final String SHA_256 = "SHA-256";

    /**
     * FIPS PUB 180-2 中定义的 SHA-384 哈希算法。
     */
    public static final String SHA_384 = "SHA-384";

    /**
     * FIPS PUB 180-2 中定义的 SHA-512 哈希算法。
     */
    public static final String SHA_512 = "SHA-512";

    /**
     * FIPS PUB 180-4 中定义的 SHA-512 哈希算法。
     * <p>从 Oracle Java 9 开始包含。</p>
     *
     * @since 1.14
     */
    public static final String SHA_512_224 = "SHA-512/224";

    /**
     * FIPS PUB 180-4 中定义的 SHA-512 哈希算法。
     * <p>从 Oracle Java 9 开始包含。</p>
     */
    public static final String SHA_512_256 = "SHA-512/256";

    /**
     * FIPS PUB 202 中定义的 SHA3-224 哈希算法。
     * <p>Included starting in Oracle Java 9.</p>
     */
    public static final String SHA3_224 = "SHA3-224";

    /**
     * FIPS PUB 202 中定义的 SHA3-256 哈希算法。
     * <p>Included starting in Oracle Java 9.</p>
     */
    public static final String SHA3_256 = "SHA3-256";

    /**
     * FIPS PUB 202 中定义的 SHA3-384 哈希算法。
     * <p>Included starting in Oracle Java 9.</p>
     */
    public static final String SHA3_384 = "SHA3-384";

    /**
     * FIPS PUB 202 中定义的 SHA3-512 哈希算法。
     * <p>从 Oracle Java 9 开始包含。</p>
     */
    public static final String SHA3_512 = "SHA3-512";

    /**
     * 获取此类中定义的所有常量值。
     *
     * @return 此类中定义的所有常量值。
     */
    public static String[] values() {
        // 不要在这里使用常量数组，因为它可能会因意外或设计而从外部更改
        return new String[] {
                MD2, MD5, SHA_1, SHA_224, SHA_256, SHA_384,
                SHA_512, SHA_512_224, SHA_512_256, SHA3_224, SHA3_256, SHA3_384, SHA3_512
        };
    }

}
