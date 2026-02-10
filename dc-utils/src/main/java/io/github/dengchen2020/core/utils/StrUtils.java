package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

/**
 * 字符串工具类
 * @author xiaochen
 * @since 2022/8/19
 */
@NullMarked
public abstract class StrUtils {

    /**
     * 字符串格式化输出
     * @param strPattern 含{}占位符的字符串
     * @param args 参数
     * @return 格式化后的字符串
     */
    public static String format(String strPattern, Object... args){
        return MessageFormatter.arrayFormat(strPattern,args).getMessage();
    }

    /**
     * 生成uuid
     * @return uuid字符串
     */
    public static String uuid(){
        return UUID.randomUUID().toString();
    }

    /**
     * 生成uuid简化版
     * @return 去除-后的uuid字符串
     */
    public static String uuidSimplified(){
        return UUID.randomUUID().toString().replace("-","");
    }

    /**
     * 当字段存json数组字符串时，做预处理
     * @param jsonStr json数组字符串
     * @return json数组字符串
     */
    public static String pretreatmentJsonArray(String jsonStr) {
        if (!hasText(jsonStr)) return "[]";
        return jsonStr;
    }

    /**
     * 当字段存json对象字符串时，做预处理
     * @param jsonStr json对象字符串
     * @return json对象字符串
     */
    public static String pretreatmentJsonObject(String jsonStr) {
        if (!hasText(jsonStr)) return "{}";
        return jsonStr;
    }

    /**
     * 按指定字节数截断字符串
     * @param str 字符串
     * @param targetByteLength 目标字节数
     * @return 按字节数截断后的字符串
     */
    public static String shortenString(String str, int targetByteLength) {
        if (targetByteLength <= 0) return "";
        try {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            if (bytes.length <= targetByteLength) return str;
            // 找到最大字节位置，确保不截断多字节字符
            int maxByteIndex = 0;
            for (int i = 0; i < bytes.length && i < targetByteLength; ) {
                int charByteLength;
                if ((bytes[i] & 0x80) == 0) { // 0xxxxxxx - 1字节字符 (ASCII)
                    charByteLength = 1;
                } else if ((bytes[i] & 0xE0) == 0xC0) { // 110xxxxx - 2字节字符
                    charByteLength = 2;
                } else if ((bytes[i] & 0xF0) == 0xE0) { // 1110xxxx - 3字节字符
                    charByteLength = 3;
                } else if ((bytes[i] & 0xF8) == 0xF0) { // 11110xxx - 4字节字符
                    charByteLength = 4;
                } else {
                    // 无效的UTF-8字节，按1字节处理
                    charByteLength = 1;
                }
                // 如果加上当前字符会超出目标长度，则停止
                if (i + charByteLength > targetByteLength) break;
                maxByteIndex = i + charByteLength;
                i = maxByteIndex;
            }
            // 使用字节数组构造截断后的字符串
            return new String(bytes, 0, maxByteIndex, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * 检查 CharSequence 是否仅包含 Unicode 数字。小数点不是 Unicode 数字，返回 false。
     * null 将返回 false.一个空的 CharSequence （length（）=0） 将返回 false。
     * 请注意，该方法不允许使用正号或负号。此外，如果 String 通过了数值测试，则在由 Integer.parseInt 或 Long.parseLong 解析时，它仍可能生成 NumberFormatException，例如，如果值分别超出 int 或 long 的范围。
     *
     * <pre>
     * isNumeric(null)   = false
     * isNumeric("")     = false
     * isNumeric("  ")   = false
     * isNumeric("123")  = true
     * isNumeric("१२३")  = true
     * isNumeric("12 3") = false
     * isNumeric("ab2c") = false
     * isNumeric("12-3") = false
     * isNumeric("12.3") = false
     * isNumeric("-123") = false
     * isNumeric("+123") = false
     * </pre>
     *
     * @param cs  要检查的 CharSequence，可能为 null
     * @return {@code true} 如果仅包含数字，并且为非空值
     */
    public static boolean isNumeric(final CharSequence cs) {
        if (!hasText(cs)) return false;
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) return false;
        }
        return true;
    }

    /**
     * 转换为十六进制字符串
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String formatHex(byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * 防止数组越界，安全访问指定索引的数据，访问不到时返回空字符串
     * @param arr 字符串数组
     * @param i 索引
     * @return 值
     */
    public static String getValue(String[] arr, int i) {
        if (i >= 0 && i < arr.length) return arr[i];
        return "";
    }

    /**
     * 检查给定的{@code str}是否包含实际<em>文本</em>。
     * <p>更具体地说，如果
     * {@code str}不是{@code null}，其长度大于0，
     * 并且至少包含一个非空白字符。
     * @param str 检查{@code String} （可能是 {@code null}）
     * @return {@code true} 如果 {@code str} 不是 {@code null}，且其长度大于 0，且不包含仅有空白
     */
    public static boolean hasText(@Nullable CharSequence str) {
        if (str == null) return false;
        int strLen = str.length();
        if (strLen == 0) return false;
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) return true;
        }
        return false;
    }

    /**
     * 检查给定的{@code str}是否包含实际<em>文本</em>。
     * <p>更具体地说，如果
     * {@code str}不是{@code null}，其长度大于0，
     * 并且至少包含一个非空白字符。
     * @param str 检查{@code String} （可能是 {@code null}）
     * @return {@code true} 如果 {@code str} 不是 {@code null}，且其长度大于 0，且不包含仅有空白
     */
    public static boolean hasText(@Nullable String str) {
        return (str != null && !str.isBlank());
    }
}
