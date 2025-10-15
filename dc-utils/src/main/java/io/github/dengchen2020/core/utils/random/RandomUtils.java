package io.github.dengchen2020.core.utils.random;

import org.springframework.util.Assert;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 生成随机数的工具类
 * @author xiaochen
 * @since 2024/12/26
 */
public class RandomUtils {

    private static final RandomUtils INSECURE = new RandomUtils(ThreadLocalRandom::current);

    private static final RandomUtils SECURE = new RandomUtils(SecureRandom::new);

    private static final ThreadLocal<SecureRandom> SECURE_RANDOM_STRONG = ThreadLocal.withInitial(() -> {
        try {
            return SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    });

    private static final Supplier<Random> SECURE_STRONG_SUPPLIER = SECURE_RANDOM_STRONG::get;

    private static final RandomUtils SECURE_STRONG = new RandomUtils(SECURE_STRONG_SUPPLIER);

    /**
     * 获取单例实例的依据 {@link ThreadLocalRandom#current()}; <b>它不是加密的
     * 安全</b>;用 {@link #secure()} 要使用
     * {@code securerandom.strongAlgorithms} {@link Security} 属性
     * <p>方法 {@link ThreadLocalRandom#current()} 称为按需。</p>
     *
     * @return 基于 {@link ThreadLocalRandom#current()}.
     * @see ThreadLocalRandom#current()
     * @see #secure()
     */
    public static RandomUtils insecure() {
        return INSECURE;
    }

    /**
     * 获取单例实例的依据 {@link SecureRandom#SecureRandom()} 它使用算法/提供程序
     * 在 {@code securerandom.strongAlgorithms} {@link Security} 属性
     * <p>方法 {@link SecureRandom#SecureRandom()} 称为按需。</p>
     *
     * @return 基于 {@link SecureRandom#SecureRandom()}.
     * @see SecureRandom#SecureRandom()
     */
    public static RandomUtils secure() {
        return SECURE;
    }

    static SecureRandom secureRandom() {
        return SECURE_RANDOM_STRONG.get();
    }

    /**
     * 获取单例实例的依据 {@link SecureRandom#getInstanceStrong()} 它使用算法/提供程序
     * 在 {@code securerandom.strongAlgorithms} {@link Security} 属性
     * <p>方法 {@link SecureRandom#getInstanceStrong()} 称为按需。</p>
     *
     * @return 基于 {@link SecureRandom#getInstanceStrong()}.
     * @see SecureRandom#getInstanceStrong()
     */
    public static RandomUtils secureStrong() {
        return SECURE_STRONG;
    }

    private final Supplier<Random> random;

    private RandomUtils(final Supplier<Random> random) {
        this.random = random;
    }

    Random random() {
        return random.get();
    }

    /**
     * 生成随机布尔值。
     *
     * @return 随机布尔值
     */
    public boolean randomBoolean() {
        return random().nextBoolean();
    }

    /**
     * 生成随机字节数组。
     *
     * @param count 计算返回数组的大小
     * @return 随机字节数组
     * @throws IllegalArgumentException if {@code count} is negative
     */
    public byte[] randomBytes(final int count) {
        Assert.isTrue(count >= 0, "Count cannot be negative.");
        final byte[] result = new byte[count];
        random().nextBytes(result);
        return result;
    }

    /**
     * 生成介于 0（含）和 Double.MAX_VALUE（不含）之间的随机双精度值。
     *
     * @return 随机双精度
     * @see #randomDouble(double, double)
     */
    public double randomDouble() {
        return randomDouble(0, Double.MAX_VALUE);
    }

    /**
     * 在指定范围内生成随机双精度。
     *
     * @param startInclusive 可返回的最小值，必须为非负数
     * @param endExclusive（不包括在内）
     * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或 {@code startInclusive} 为阴性
     * @return 随机双精度小数
     */
    public double randomDouble(final double startInclusive, final double endExclusive) {
        Assert.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Assert.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endExclusive) return startInclusive;
        return startInclusive + (endExclusive - startInclusive) * random().nextDouble();
    }

    /**
     * 生成介于 0（含）和 Float.MAX_VALUE（不含）之间的随机浮点数。
     *
     * @return 随机浮点数
     * @see #randomFloat(float, float)
     */
    public float randomFloat() {
        return randomFloat(0, Float.MAX_VALUE);
    }

    /**
     * 在指定范围内生成随机浮点数。
     *
     * @param startInclusive 可以返回的最小值必须为非负值
     * @param endExclusive   上限（不包括在内）
     * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或 {@code startInclusive} 为负数
     * @return 随机浮点数
     */
    public float randomFloat(final float startInclusive, final float endExclusive) {
        Assert.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Assert.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endExclusive) return startInclusive;
        return startInclusive + (endExclusive - startInclusive) * random().nextFloat();
    }

    /**
     * 生成一个介于 0（含）和 Integer.MAX_VALUE（不含）之间的随机 int。
     *
     * @return 随机整数
     * @see #randomInt(int, int)
     */
    public int randomInt() {
        return randomInt(0, Integer.MAX_VALUE);
    }

    /**
     * 生成指定范围内的随机整数。
     *
     * @param startInclusive 可以返回的最小值必须为非负值
     * @param endExclusive   上限（不包括在内）
     * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或 {@code startInclusive} 为负数
     * @return 随机整数
     */
    public int randomInt(final int startInclusive, final int endExclusive) {
        Assert.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Assert.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endExclusive) return startInclusive;
        return startInclusive + random().nextInt(endExclusive - startInclusive);
    }

    /**
     * 生成一个介于 0（含）和 Long.MAX_VALUE（不含）之间的随机 long。
     *
     * @return the random long
     * @see #randomLong(long, long)
     */
    public long randomLong() {
        return randomLong(Long.MAX_VALUE);
    }

    /**
     * 生成一个介于 0（含）和指定值（不含）之间的 {@code long} 值。
     *
     * @param n 绑定要返回的随机数。必须为正数。
     * @return a random {@code long} 值介于 0（含）和 {@code n}（不包括）之间。
     */
    private long randomLong(final long n) {
        // Extracted from o.a.c.rng.core.BaseProvider.nextLong(long)
        long bits;
        long val;
        do {
            bits = random().nextLong() >>> 1;
            val = bits % n;
        } while (bits - val + n - 1 < 0);
        return val;
    }

    /**
     * 在指定范围内生成一个随机 long。
     *
     * @param startInclusive 可以返回的最小值必须为非负值
     * @param endExclusive   上限（不包括在内）
     * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或 {@code startInclusive} 为负数
     * @return 随机 long
     */
    public long randomLong(final long startInclusive, final long endExclusive) {
        Assert.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Assert.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endExclusive) return startInclusive;
        return startInclusive + randomLong(endExclusive - startInclusive);
    }

    @Override
    public String toString() {
        return "RandomUtils [random=" + random() + "]";
    }

}
