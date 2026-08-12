package io.github.dengchen2020.core.utils.language;

/**
 * 语言检测
 * <p>字符级文字归类工具，不做文本级语言识别。判别策略：
 * <ul>
 *   <li>ASCII 快速路径覆盖最常见的英文/数字/半角符号；</li>
 *   <li>主语种由 {@link Character.UnicodeScript} 识别；HAN 内部使用区段区分简繁；</li>
 *   <li>数字/符号/空白依靠 {@link Character} 的 General Category 与 isWhitespace。</li>
 * </ul>
 *
 * @author xiaochen
 * @since 2026/5/20
 */
public final class LangDetector {

    private LangDetector() {
    }

    /**
     * P 族（Punctuation）+ S 族（Symbol）的 General Category 位掩码。
     */
    private static final int PUNCT_SYMBOL_MASK =
            (1 << Character.CONNECTOR_PUNCTUATION)
                    | (1 << Character.DASH_PUNCTUATION)
                    | (1 << Character.START_PUNCTUATION)
                    | (1 << Character.END_PUNCTUATION)
                    | (1 << Character.INITIAL_QUOTE_PUNCTUATION)
                    | (1 << Character.FINAL_QUOTE_PUNCTUATION)
                    | (1 << Character.OTHER_PUNCTUATION)
                    | (1 << Character.MATH_SYMBOL)
                    | (1 << Character.CURRENCY_SYMBOL)
                    | (1 << Character.MODIFIER_SYMBOL)
                    | (1 << Character.OTHER_SYMBOL);

    /**
     * 语言判断
     *
     * @param codePoint Unicode 码点
     * @return 语言类型
     */
    public static Lang getLang(int codePoint) {
        // ASCII 快速路径：覆盖最常见的英文/数字/半角符号
        if (codePoint < 0x80) {
            if (isEnglishLetter(codePoint)) return Lang.EN;
            if (codePoint >= '0' && codePoint <= '9') return Lang.NUMBER;
            return isSymbol(codePoint) ? Lang.SYMBOL : Lang.OTHER;
        }
        return switch (Character.UnicodeScript.of(codePoint)) {
            case HAN -> isZhCn(codePoint) ? Lang.ZH_CN : Lang.ZH_TW;
            case CYRILLIC -> Lang.RU;
            case LATIN -> Lang.EN;
            default -> {
                if (Character.isDigit(codePoint)) yield Lang.NUMBER;
                if (isSymbol(codePoint)) yield Lang.SYMBOL;
                yield Lang.OTHER;
            }
        };
    }

    /**
     * 简体中文
     * <p>仅覆盖 CJK 统一汉字基本区（0x4E00–0x9FFF）。该区段中也存在大量繁体字（如「國」「愛」「學」），
     * Unicode 不区分简繁，此处以该区作为“简体”与“扩展/繁体”的约定分界。</p>
     *
     * @param cp 待判断码点
     * @return 是否在 CJK 基本区
     */
    public static boolean isZhCn(int cp) {
        return cp >= 0x4E00 && cp <= 0x9FFF;
    }

    /**
     * 纯 ASCII 英文字母
     *
     * @param cp 待判断码点
     * @return 是否为 ASCII 英文字母
     */
    public static boolean isEnglishLetter(int cp) {
        return (cp >= 'a' && cp <= 'z') || (cp >= 'A' && cp <= 'Z');
    }

    /**
     * 标点或符号（涵盖中英文标点、货币/运算/修饰/其他符号）
     * <p>判定依据 Unicode General Category：P 族（Pc/Pd/Ps/Pe/Pi/Pf/Po）与 S 族（Sm/Sc/Sk/So）。</p>
     *
     * @param cp 待判断码点
     * @return 是否为标点或符号
     */
    public static boolean isPunctOrSymbol(int cp) {
        return (PUNCT_SYMBOL_MASK & (1 << Character.getType(cp))) != 0;
    }

    /**
     * 判断是否为符号（标点/符号 + 空白符）
     *
     * @param cp 待判断码点
     * @return 是否为符号
     */
    public static boolean isSymbol(int cp) {
        return Character.isWhitespace(cp) || isPunctOrSymbol(cp);
    }

}
