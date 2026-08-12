package io.github.dengchen2020.core.utils.random;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 生成随机字符串的工具类
 * @author xiaochen
 * @since 2024/12/26
 */
public class RandomStringUtils {

    private static final Supplier<RandomUtils> SECURE_SUPPLIER = RandomUtils::secure;

    private static final RandomStringUtils INSECURE = new RandomStringUtils(RandomUtils::insecure);

    private static final RandomStringUtils SECURE = new RandomStringUtils(SECURE_SUPPLIER);

    private static final RandomStringUtils SECURE_STRONG = new RandomStringUtils(RandomUtils::secureStrong);

    private static final char[] ALPHANUMERICAL_CHARS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G',
            'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1',
            '2', '3', '4', '5', '6', '7', '8', '9' };

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
    public static RandomStringUtils insecure() {
        return INSECURE;
    }

    /**
     * 使用提供的随机性源，基于各种选项创建随机字符串。
     *
     * <p>
     * 如果 start 和 end 都是 {@code 0}, start 和 end 设置为 {@code ' '} and {@code 'z'}, ASCII 可打印对象
     * 字符, 将使用，除非字母和数字都是 {@code false}, 在这种情况下，将设置 start 和 end
     * 自 {@code 0} and {@link Character#MAX_CODE_POINT}.
     *
     * <p>如果 set 不是 {@code null}，则选择 start 和 end 之间的字符。</p>
     *
     * <p>
     * 此方法接受用户提供的 {@link Random} 实例用作随机性源。通过为单个
     * {@link Random} 实例，并将其用于每次调用，则相同的随机字符串序列可以是
     * 重复且可预测地生成。
     * </p>
     *
     * @param count   要创建的 random 字符串的长度
     * @param start   开始于 （含） 的字符集中的位置
     * @param end     字符集中要结束的位置（不包括）
     * @param letters 如果 {@code true}，则生成的字符串可能包含字母字符
     * @param numbers 如果 {@code true}，则生成的字符串可能包含数字字符
     * @param chars   要从中选择 Randoms 的字符集不能为空。如果 {@code null}，则它将使用
     * 所有字符的集合。
     * @param random  随机性的来源。
     * @return 随机字符串
     * @throws ArrayIndexOutOfBoundsException 如果 set 数组中没有 {@code （end - start） + 1} 个字符。
     * @throws IllegalArgumentException       如果 {@code count} &lt; 0 或提供的 chars 数组为空。
     */
    public static String random(int count, int start, int end, final boolean letters, final boolean numbers,
                                final char[] chars, final Random random) {
        if (count == 0) {
            return "";
        }
        if (count < 0) {
            throw new IllegalArgumentException("Requested random string length " + count + " is less than 0.");
        }
        if (chars != null && chars.length == 0) {
            throw new IllegalArgumentException("The chars array must not be empty");
        }

        if (start == 0 && end == 0) {
            if (chars != null) {
                end = chars.length;
            } else if (!letters && !numbers) {
                end = Character.MAX_CODE_POINT;
            } else {
                end = 'z' + 1;
                start = ' ';
            }
        } else if (end <= start) {
            throw new IllegalArgumentException(
                    "Parameter end (" + end + ") must be greater than start (" + start + ")");
        } else if (start < 0 || end < 0) {
            throw new IllegalArgumentException("Character positions MUST be >= 0");
        }

        if (end > Character.MAX_CODE_POINT) {
            // 从技术上讲，它应该是 'Character.MAX_CODE_POINT+1'，因为 'end' 被排除在外
            // 但是字符 'Character.MAX_CODE_POINT' 是私人使用的，所以它无论如何都会被排除在外
            end = Character.MAX_CODE_POINT;
        }

        // 优化完整字母数字字符的生成
        // 通常，我们需要选择一个 7 位整数，因为 gap = 'z' - '0' + 1 = 75 > 64
        // 反过来，这将使我们以概率拒绝采样 1 - 62 / 2^7 > 1 / 2
        // 相反，我们可以直接从正确的 62 个字符集中选择，这需要
        // 选择一个 6 位整数，并且只以概率拒绝 2 / 64 = 1 / 32
        if (chars == null && letters && numbers && start <= '0' && end >= 'z' + 1) {
            return random(count, 0, 0, false, false, ALPHANUMERICAL_CHARS, random);
        }

        // 优化按字母和/或数字筛选时的 start 和 end：
        // 提供的范围可能太大，因为之后我们仍然会进行过滤。
        // 请注意 Math.min/max 的使用（例如，与将 start 设置为 '0' 相反），
        // 由于范围 start/end 可能不包括一些字母/数字，
        // 例如，当 numbers = true 时，Start Already 为 '1'，并且 Start
        // 在这种情况下，需要保持等于 '1'。
        if (chars == null) {
            if (letters && numbers) {
                start = Math.max('0', start);
                end = Math.min('z' + 1, end);
            } else if (numbers) {
                // 只有数字，没有字母
                start = Math.max('0', start);
                end = Math.min('9' + 1, end);
            } else if (letters) {
                // 只有字母，没有数字
                start = Math.max('A', start);
                end = Math.min('z' + 1, end);
            }
        }

        final int zeroDigitAscii = 48;
        final int firstLetterAscii = 65;

        if (chars == null && (numbers && end <= zeroDigitAscii || letters && end <= firstLetterAscii)) {
            throw new IllegalArgumentException(
                    "Parameter end (" + end + ") must be greater then (" + zeroDigitAscii + ") for generating digits "
                            + "or greater then (" + firstLetterAscii + ") for generating letters.");
        }

        final StringBuilder builder = new StringBuilder(count);
        final int gap = end - start;
        final int gapBits = Integer.SIZE - Integer.numberOfLeadingZeros(gap);
        // 我们使用的缓存大小是启发式的：
        // 如果没有拒绝，则大约是所需字节数的两倍
        // 理想情况下，缓存大小取决于多个因素，包括生成 x 字节的成本
        // 随机性以及拒绝的可能性。然而，这并不容易知道
        // 对于一般情况，这些值以编程方式进行。
        final CachedRandomBits arb = new CachedRandomBits((count * gapBits + 3) / 5 + 10, random);

        while (count-- != 0) {
            // 在 start （included） 和 end （excluded） 之间生成一个随机值
            final int randomValue = arb.nextBits(gapBits) + start;
            // 如果值太大，则拒绝采样
            if (randomValue >= end) {
                count++;
                continue;
            }

            final int codePoint;
            if (chars == null) {
                codePoint = randomValue;

                switch (Character.getType(codePoint)) {
                    case Character.UNASSIGNED:
                    case Character.PRIVATE_USE:
                    case Character.SURROGATE:
                        count++;
                        continue;
                }

            } else {
                codePoint = chars[randomValue];
            }

            final int numberOfChars = Character.charCount(codePoint);
            if (count == 0 && numberOfChars > 1) {
                count++;
                continue;
            }

            if (letters && Character.isLetter(codePoint) || numbers && Character.isDigit(codePoint)
                    || !letters && !numbers) {
                builder.appendCodePoint(codePoint);

                if (numberOfChars == 2) {
                    count--;
                }

            } else {
                count++;
            }
        }
        return builder.toString();
    }

    /**
     * 获取单例实例的依据 {@link SecureRandom#SecureRandom()} 它使用一个安全的随机数生成器 （RNG） 来实现默认的
     * random number 算法。
     * <p>方法 {@link SecureRandom#SecureRandom()} 称为按需。</p>
     *
     * @return 基于 {@link SecureRandom#SecureRandom()}.
     * @see SecureRandom#SecureRandom()
     */
    public static RandomStringUtils secure() {
        return SECURE;
    }

    /**
     * 获取单例实例的依据 {@link SecureRandom#getInstanceStrong()} 它使用算法/提供程序
     * 在 {@code securerandom.strongAlgorithms} {@link Security} 属性
     * <p>方法 {@link SecureRandom#getInstanceStrong()} 称为按需。</p>
     *
     * @return 基于 {@link SecureRandom#getInstanceStrong()}.
     * @see SecureRandom#getInstanceStrong()
     */
    public static RandomStringUtils secureStrong() {
        return SECURE_STRONG;
    }

    private final Supplier<RandomUtils> random;

    private RandomStringUtils(final Supplier<RandomUtils> random) {
        this.random = random;
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>将从所有字符集中选择字符。</p>
     *
     * @param count 要创建的 random 字符串的长度
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     */
    public String next(final int count) {
        return next(count, false, false);
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>字符将从参数指示的字母数字字符集中选择。</p>
     *
     * @param count   要创建的 random 字符串的长度
     * @param letters if {@code true}, 生成的字符串可以包含字母字符
     * @param numbers if {@code true}, 生成的字符串可能包含数字字符
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     */
    public String next(final int count, final boolean letters, final boolean numbers) {
        return next(count, 0, 0, letters, numbers);
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>将从指定的字符集中选择字符。</p>
     *
     * @param count 要创建的 random 字符串的长度
     * @param chars 包含要使用的字符集的字符数组可能为 null
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     */
    public String next(final int count, final char... chars) {
        if (chars == null) {
            return random(count, 0, 0, false, false, null, random());
        }
        return random(count, 0, chars.length, false, false, chars, random());
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>字符将从参数指示的字母数字字符集中选择。</p>
     *
     * @param count   要创建的 random 字符串的长度
     * @param start   开始于 （含） 的字符集中的位置
     * @param end     在字符集中的结束位置之前的位置 (不包含)
     * @param letters if {@code true}, 生成的字符串可以包含字母字符
     * @param numbers if {@code true}, 生成的字符串可能包含数字字符
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     * @since 3.16.0
     */
    public String next(final int count, final int start, final int end, final boolean letters, final boolean numbers) {
        return random(count, start, end, letters, numbers, null, random());
    }

    /**
     * 使用默认随机性源，基于各种选项创建随机字符串。
     *
     * <p>
     * 此方法具有与 {@link #random(int,int,int,boolean,boolean,char[],Random)}, 但
     * 它使用内部静态 {@link Random}
     * 实例
     * </p>
     *
     * @param count   要创建的 random 字符串的长度
     * @param start   开始于 （含） 的字符集中的位置
     * @param end     在字符集中的结束位置之前的位置 (不包含)
     * @param letters 如果 {@code true}，则生成的字符串可能包含字母字符
     * @param numbers 如果 {@code true}，则生成的字符串可能包含数字字符
     * @param chars   要从中选择 Random 的字符集。如果 {@code null}，则它将使用所有字符的集合。
     * @return 随机字符串
     * @throws ArrayIndexOutOfBoundsException 如果 set 数组中没有 {@code （end - start） + 1} 个字符。
     * @throws IllegalArgumentException       如果 {@code count} 小于 0。
     */
    public String next(final int count, final int start, final int end, final boolean letters, final boolean numbers,
                       final char... chars) {
        return random(count, start, end, letters, numbers, chars, random());
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>字符将从字符串指定的字符集中选择，不能为空。如果为 null，则设置的字符。</p>
     *
     * @param count 要创建的 random 字符串的长度
     * @param chars 包含要使用的字符集的 String 可以为 null，但不能为空
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0 或字符串为空。
     */
    public String next(final int count, final String chars) {
        if (chars == null) {
            return random(count, 0, 0, false, false, null, random());
        }
        return next(count, chars.toCharArray());
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>字符将从拉丁字母字符集中选择 (a-z, A-Z).</p>
     *
     * @param count 要创建的 random 字符串的长度
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     */
    public String nextAlphabetic(final int count) {
        return next(count, true, false);
    }

    /**
     * 创建一个随机字符串，其长度介于非独占最小值和独占最大值之间。
     *
     * <p>字符将从拉丁字母字符集中选择 (a-z, A-Z).</p>
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的独占最大长度
     * @return 随机字符串
     */
    public String nextAlphabetic(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextAlphabetic(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>字符将从拉丁字母字符集中选择 (a-z, A-Z) 和数字 0-9.</p>
     *
     * @param count 要创建的 random 字符串的长度
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     */
    public String nextAlphanumeric(final int count) {
        return next(count, true, true);
    }

    /**
     * 创建一个随机字符串，其长度介于非独占最小值和独占最大值之间。
     *
     * <p>字符将从拉丁字母字符集中选择 (a-z, A-Z) 和数字 0-9.</p>
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的独占最大长度
     * @return 随机字符串
     */
    public String nextAlphanumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextAlphanumeric(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>将从 ASCII 值介于 {@code 32} and {@code 126}(包括).</p>
     *
     * @param count 要创建的 random 字符串的长度
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     */
    public String nextAscii(final int count) {
        return next(count, 32, 127, false, false);
    }

    /**
     * 创建一个随机字符串，其长度介于非独占最小值和独占最大值之间。
     *
     * <p>将从 ASCII 值介于 {@code 32} 和 {@code 126} 之间的字符集中选择字符(包括).</p>
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的独占最大长度
     * @return 随机字符串
     */
    public String nextAscii(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextAscii(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>将从与 POSIX [：graph：] 正则表达式字符匹配的字符集中选择字符类。此类包含所有可见的 ASCII 字符（即除空格和控制字符之外的任何内容）。</p>
     *
     * @param count 要创建的 random 字符串的长度
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     */
    public String nextGraph(final int count) {
        return next(count, 33, 126, false, false);
    }

    /**
     * 创建一个随机字符串，其长度介于非独占最小值和独占最大值之间。
     *
     * <p>字符将从 \p{Graph} 字符集中选择。</p>
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的独占最大长度
     * @return 随机字符串
     */
    public String nextGraph(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextGraph(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>将从数字字符集中选择字符。</p>
     *
     * @param count 要创建的 random 字符串的长度
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     */
    public String nextNumeric(final int count) {
        return next(count, false, true);
    }

    /**
     * 创建一个随机字符串，其长度介于非独占最小值和独占最大值之间。
     *
     * <p>字符将从 \p{Digit} 字符集中选择。</p>
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的独占最大长度
     * @return 随机字符串
     */
    public String nextNumeric(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextNumeric(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * 创建一个随机字符串，其长度为指定的字符数。
     *
     * <p>将从与 POSIX [:p rint：] 正则表达式字符匹配的字符集中选择字符类。此类包括所有可见的 ASCII 字符和空格（即除控制字符之外的任何内容）。</p>
     *
     * @param count 要创建的 random 字符串的长度
     * @return 随机字符串
     * @throws IllegalArgumentException 如果 {@code count} 小于 0。
     */
    public String nextPrint(final int count) {
        return next(count, 32, 126, false, false);
    }

    /**
     * 创建一个随机字符串，其长度介于非独占最小值和独占最大值之间。
     *
     * <p>字符将从 \p{Print} 字符集中选择。</p>
     *
     * @param minLengthInclusive 要生成的字符串的最小长度
     * @param maxLengthExclusive 要生成的字符串的独占最大长度
     * @return 随机字符串
     */
    public String nextPrint(final int minLengthInclusive, final int maxLengthExclusive) {
        return nextPrint(randomUtils().randomInt(minLengthInclusive, maxLengthExclusive));
    }

    /**
     * 获取 {@link Random}实例
     *
     * @return {@link Random}实例
     */
    private Random random() {
        return randomUtils().random();
    }

    /**
     * 获取 {@link RandomUtils}实例
     *
     * @return {@link RandomUtils}实例
     */
    private RandomUtils randomUtils() {
        return random.get();
    }

    @Override
    public String toString() {
        return "RandomStringUtils [random=" + random() + "]";
    }

}
