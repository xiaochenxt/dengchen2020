package io.github.dengchen2020.core.utils.language;

/**
 * 语言片段处理器
 *
 * @author xiaochen
 * @since 2026/5/20
 */
public class LangSegmentProcessor {

    /**
     * 回调：处理不同语言的片段
     */
    @FunctionalInterface
    public interface LangSegmentHandler {
        /**
         * @param lang    片段语言
         * @param content 片段内容
         * @param start   起始下标（基于 char，闭区间）
         * @param end     结束下标（基于 char，开区间）
         * @return 替换后的内容；返回 {@code null} 时该片段不写入结果
         */
        String handle(Lang lang, String content, int start, int end);
    }

    /**
     * 处理语言片段
     * <p>基于 Unicode code point 迭代，正确处理辅助平面字符（如 emoji、扩展汉字）。</p>
     *
     * @param input   输入字符串，为 {@code null} 或空串时返回空串
     * @param handler 片段回调
     * @return 处理后结果
     */
    public static String process(String input, LangSegmentHandler handler) {
        if (input == null || input.isEmpty()) return "";
        int length = input.length();
        StringBuilder sb = new StringBuilder(length);
        int index = 0;
        int cp = input.codePointAt(index);
        Lang currentLang = LangDetector.getLang(cp);
        while (index < length) {
            int start = index;
            Lang lang = currentLang;
            // 符号：单个字符单独回调
            if (lang == Lang.SYMBOL) {
                index += Character.charCount(cp);
            } else {
                do {
                    index += Character.charCount(cp);
                    if (index >= length) break;
                    cp = input.codePointAt(index);
                    currentLang = LangDetector.getLang(cp);
                } while (currentLang == lang);
            }
            if (lang == Lang.SYMBOL && index < length) {
                cp = input.codePointAt(index);
                currentLang = LangDetector.getLang(cp);
            }
            String content = input.substring(start, index);
            String result = handler.handle(lang, content, start, index);
            if (result != null) sb.append(result);
        }
        return sb.toString();
    }

}
