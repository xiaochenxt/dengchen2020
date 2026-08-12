package io.github.dengchen2020.core.utils.random;

import java.util.Objects;
import java.util.Random;

/**
 * 生成特定位长的随机整数。
 *
 * <p>
 * 这比调用 Random.nextInt(1 << nbBits). 它使用 cacheSize 随机字节的缓存，当它变空时，它会补充该缓存。这是
 * 特别有利于 SecureRandom Drbg 实现，因为每次生成随机数都会产生恒定的成本。
 * </p>
 * <p>内部使用者 {@link RandomStringUtils}</p>
 * <p>线程不安全</p>
 */
final class CachedRandomBits {

    private final Random random;

    private final byte[] cache;

    /**
     * 高速缓存中要使用的下一个位的索引。
     *
     * <ul>
     * <li>bitIndex=0 表示缓存是完全随机的，并且尚未使用任何位。</li>
     * <li>bitIndex=1 表示只使用了 cache[0] 的 LSB，其他所有位都可以使用。</li>
     * <li>bitIndex=8 表示只使用了 cache[0] 的 8 位。</li>
     * </ul>
     */
    private int bitIndex;

    /**
     * Creates a new instance.
     *
     * @param cacheSize 缓存的字节数（仅影响性能）
     * @param random 随机源
     */
    CachedRandomBits(final int cacheSize, final Random random) {
        if (cacheSize <= 0) {
            throw new IllegalArgumentException("cacheSize must be positive");
        }
        this.cache = new byte[cacheSize];
        this.random = Objects.requireNonNull(random, "random");
        this.random.nextBytes(this.cache);
        this.bitIndex = 0;
    }

    /**
     * 生成具有指定位数的随机整数。
     *
     * @param bits 要生成的位数，必须介于 1 到 32 之间
     * @return 具有 {@code 位} 位的随机整数
     */
    public int nextBits(final int bits) {
        if (bits > 32 || bits <= 0) {
            throw new IllegalArgumentException("number of bits must be between 1 and 32");
        }
        int result = 0;
        int generatedBits = 0; // number of generated bits up to now
        while (generatedBits < bits) {
            if (bitIndex >> 3 >= cache.length) {
                // we exhausted the number of bits in the cache
                // this should only happen if the bitIndex is exactly matching the cache length
                assert bitIndex == cache.length * 8;
                random.nextBytes(cache);
                bitIndex = 0;
            }
            // generatedBitsInIteration is the number of bits that we will generate
            // in this iteration of the while loop
            int generatedBitsInIteration = Math.min(8 - (bitIndex & 0x7), bits - generatedBits);
            result = result << generatedBitsInIteration;
            result |= (cache[bitIndex >> 3] >> (bitIndex & 0x7)) & ((1 << generatedBitsInIteration) - 1);
            generatedBits += generatedBitsInIteration;
            bitIndex += generatedBitsInIteration;
        }
        return result;
    }
}
