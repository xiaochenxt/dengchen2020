package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 字符串工具类
 * @author xiaochen
 * @since 2022/8/19
 */
public abstract class StrUtils {

    /**
     * 字符串格式化输出
     * @param strPattern 含{}占位符的字符串
     * @param args 参数
     * @return 格式化后的字符串
     */
    public static String format(@NonNull String strPattern,Object... args){
        return MessageFormatter.arrayFormat(strPattern,args).getMessage();
    }

    /**
     * 是否是数组
     * @param obj 对象
     * @return true | false
     */
    private static boolean isArray(Object obj) {
        return null != obj && obj.getClass().isArray();
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
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * 当字段存json数组字符串时，做简单校验
     * @param jsonStr json数组字符串
     * @return json数组字符串
     */
    public static String checkJsonArrayStr(String jsonStr) {
        if (!hasText(jsonStr)) return "[]";
        return jsonStr;
    }

    /**
     * 根据query参数解析出key对应的value
     *
     * @param query query查询参数
     * @param key 要查询的键
     * @return key对应的value，如果不存在则返回null
     */
    public static String getValue(String query, String key) {
        if (query == null || key == null || query.isEmpty() || key.isEmpty()) return null;
        final int keyLen = key.length();
        final int queryLen = query.length();
        int currentKeyPos = 0; // 目标key在query中的起始位置（初始为0，用于循环查找）
        // 直接定位所有可能的key位置（跳过无关参数，减少遍历）
        while ((currentKeyPos = query.indexOf(key, currentKeyPos)) != -1) {
            // 验证当前key位置是否是“独立参数”（避免匹配子串，如"username"匹配"name"）
            boolean isValidStart = (currentKeyPos == 0) || (query.charAt(currentKeyPos - 1) == '&');
            // 验证key后面是否有足够空间（至少有“=”，避免key在query末尾的情况）
            boolean hasSpaceForEqual = (currentKeyPos + keyLen) < queryLen;
            if (isValidStart && hasSpaceForEqual) {
                // 验证key后面是否是“=”（确保是key=value格式，而非key&xxx）
                if (query.charAt(currentKeyPos + keyLen) == '=') {
                    int valueStart = currentKeyPos + keyLen + 1; // value的起始位置（跳过key和=）
                    // 定位value的结束位置（下一个&或query末尾）
                    int valueEnd = query.indexOf('&', valueStart);
                    return valueEnd == -1 ? query.substring(valueStart) : query.substring(valueStart, valueEnd);
                }
            }
            // 找不到有效匹配，从下一个字符继续查找（避免死循环）
            currentKeyPos += keyLen;
            // 防止key在query末尾，currentKeyPos超出范围（极端边界）
            if (currentKeyPos >= queryLen) break;
        }
        return null;
    }

    /**
     * 缩短字符串
     * @param str 字符串
     * @param targetByteLength 目标字节数
     * @return 缩短字节后的字符串
     */
    public static String shortenString(@NonNull String str, int targetByteLength) {
        try {
            byte[] bytes = str.getBytes();
            int originalByteLength = bytes.length;
            if (originalByteLength <= targetByteLength) return str;
            int endIndex = str.length() - 1;
            int totalByteLength = originalByteLength;
            while (totalByteLength > targetByteLength && endIndex >= 0) {
                char ch = str.charAt(endIndex);
                int byteLength = ch < 128 ? 1 : 3;
                totalByteLength -= byteLength;
                endIndex--;
            }
            if (endIndex < 0) endIndex = 0;
            return str.substring(0, endIndex + 1);
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * 转化成蛇形下划线命名格式
     *
     * @param name 原字段名
     * @return 新字段名
     */
    public static String convertToSnakeCase(@NonNull String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0,len = name.length(); i < len; i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) result.append("_");
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
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
     * isNumeric("\u0967\u0968\u0969")  = true
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
    public static boolean isNumeric(final @NonNull CharSequence cs) {
        if (!hasText(cs)) return false;
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) return false;
        }
        return true;
    }

    /**
     * 将map转化为query参数，例如{@code a=1&b=2&c=3}
     * @param params map
     */
    public static String toQuery(@NonNull Map<String, ?> params) {
        StringBuilder query = new StringBuilder();
        for (Iterator<? extends Map.Entry<String, ?>> iterator = params.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, ?> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            query.append(key).append("=").append(value);
            if (iterator.hasNext()) query.append("&");
        }
        return query.toString();
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
    public static String getValue(String @NonNull[] arr, int i) {
        if (arr.length >= i + 1) return arr[i];
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
