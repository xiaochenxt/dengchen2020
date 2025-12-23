package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Map;
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
     * 检查对象是否为数组
     * @param obj 待检查的对象
     * @return 如果是数组返回true，否则返回false
     */
    public static boolean isArray(@Nullable Object obj) {
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
     * 根据query参数解析出key对应的value
     *
     * @param query query查询参数
     * @param key 要查询的键
     * @return key对应的value，如果不存在则返回null
     */
    @Nullable
    public static String getValue(@Nullable String query, @Nullable String key) {
        if (query == null || key == null || query.isEmpty() || key.isEmpty()) return null;
        final int keyLen = key.length();
        final int queryLen = query.length();
        int currentKeyPos = 0; // 目标key在query中的起始位置（初始为0，用于循环查找）
        // 直接定位所有可能的key位置（跳过无关参数，减少遍历）
        while ((currentKeyPos = query.indexOf(key, currentKeyPos)) != -1) {
            // 验证当前key位置是否是"独立参数"（避免匹配子串，如"username"匹配"name"）
            boolean isValidStart = (currentKeyPos == 0) || (query.charAt(currentKeyPos - 1) == '&');
            // 验证key后面是否有足够空间（至少有"="，避免key在query末尾的情况）
            boolean hasSpaceForEqual = (currentKeyPos + keyLen) < queryLen;
            if (isValidStart && hasSpaceForEqual) {
                // 验证key后面是否是"="（确保是key=value格式，而非key&xxx）
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
    public static String shortenString(String str, int targetByteLength) {
        if (targetByteLength <= 0) return "";
        try {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            if (bytes.length <= targetByteLength) return str;
            // 二分法查找最大的结束索引
            int left = 0;
            int right = str.length() - 1;
            int endIndex = 0;
            while (left <= right) {
                int mid = (left + right) >>> 1;
                int midBytesLen = str.substring(0, mid + 1).getBytes(StandardCharsets.UTF_8).length;
                if (midBytesLen <= targetByteLength) {
                    endIndex = mid + 1;
                    left = mid + 1;
                } else {
                    right = mid - 1;
                }
            }
            return endIndex == 0 ? "" : str.substring(0, endIndex);
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * 转化成蛇形下划线命名格式（改进版，支持更智能的转换，参考Hibernate命名策略（Hibernate是复制的SpringBoot的））
     * 例如: "userName" -> "user_name", "URLPath" -> "url_path", "XMLHttpRequest" -> "xml_http_request"
     *
     * @param name 原字段名
     * @return 新字段名
     */
    public static String convertToSpringSnakeCase(String name) {
        if (name.isEmpty()) return name;
        
        StringBuilder result = new StringBuilder();
        String input = name.replace('.', '_');
        
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            
            if (Character.isUpperCase(current)) {
                // 检查是否需要在前面添加下划线
                if (i > 0) {
                    boolean needUnderscore = false;
                    
                    // 情况1：前一个字符是小写 (如 userName 中的 u 和 N)
                    if (Character.isLowerCase(input.charAt(i - 1))) {
                        needUnderscore = true;
                    } 
                    // 情况2：当前是连续大写字母序列的结尾，且后一个字符是小写 (如 XMLHttpRequest 中的 T 和 R)
                    else if (i < input.length() - 1 && 
                             Character.isUpperCase(input.charAt(i - 1)) && 
                             Character.isLowerCase(input.charAt(i + 1))) {
                        needUnderscore = true;
                    }
                    
                    if (needUnderscore) {
                        result.append('_');
                    }
                }
                
                result.append(Character.toLowerCase(current));
            } else {
                result.append(current);
            }
        }
        
        return result.toString();
    }

    /**
     * 转化成蛇形下划线命名格式
     *
     * @param name 原字段名
     * @return 新字段名
     */
    public static String convertToSnakeCase(String name) {
        StringBuilder result = new StringBuilder();
        for (int i = 0, len = name.length(); i < len; i++) {
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
    public static boolean isNumeric(final CharSequence cs) {
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
    public static String toQuery(Map<String, ?> params) {
        StringBuilder query = new StringBuilder();
        for (Iterator<? extends Map.Entry<String, ?>> iterator = params.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, ?> entry = iterator.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            query.append(key).append("=");
            if (value != null) query.append(value);
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
