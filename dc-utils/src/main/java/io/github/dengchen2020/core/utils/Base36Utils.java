package io.github.dengchen2020.core.utils;

/**
 * Base36编解码工具类
 * <pre>
 * Base36编码使用36个字符：0-9, a-z 或 0-9, A-Z
 * 适用于生成短URL、短ID等场景
 * </pre>
 * @author xiaochen
 * @since 2025/12/13
 */
public abstract class Base36Utils {
    
    private static final char[] LOWER_CHARS = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final char[] UPPER_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int[] LOWER_INDEX = new int[128];
    private static final int[] UPPER_INDEX = new int[128];
    
    static {
        for (int i = 0; i < 128; i++) {
            LOWER_INDEX[i] = -1;
            UPPER_INDEX[i] = -1;
        }
        for (int i = 0; i < LOWER_CHARS.length; i++) {
            LOWER_INDEX[LOWER_CHARS[i]] = i;
            UPPER_INDEX[UPPER_CHARS[i]] = i;
        }
    }
    
    /**
     * 将long类型数字编码为Base36字符串（数字+小写字母）
     * @param num 待编码的数字（必须为非负数）
     * @return Base36编码字符串
     */
    public static String encodeLower(long num) {
        return encode(num, LOWER_CHARS);
    }
    
    /**
     * 将Base36字符串（数字+小写字母）解码为long类型数字
     * @param str Base36编码的字符串
     * @return 解码后的数字
     * @throws IllegalArgumentException 如果字符串包含非法字符
     */
    public static long decodeLower(String str) {
        return decode(str, LOWER_CHARS, LOWER_INDEX);
    }
    
    /**
     * 将long类型数字编码为Base36字符串（数字+大写字母）
     * @param num 待编码的数字（必须为非负数）
     * @return Base36编码字符串
     */
    public static String encodeUpper(long num) {
        return encode(num, UPPER_CHARS);
    }
    
    /**
     * 将Base36字符串（数字+大写字母）解码为long类型数字
     * @param str Base36编码的字符串
     * @return 解码后的数字
     * @throws IllegalArgumentException 如果字符串包含非法字符
     */
    public static long decodeUpper(String str) {
        return decode(str, UPPER_CHARS, UPPER_INDEX);
    }
    
    private static String encode(long num, char[] chars) {
        if (num < 0) throw new IllegalArgumentException("Input number must be non-negative: " + num);
        if (num == 0) return "0";
        
        char[] buf = new char[13];
        int pos = buf.length;
        
        while (num > 0) {
            buf[--pos] = chars[(int) (num % chars.length)];
            num /= chars.length;
        }
        
        return new String(buf, pos, buf.length - pos);
    }
    
    private static long decode(String str, char[] chars, int[] charIndex) {
        if (str == null || str.isEmpty()) throw new IllegalArgumentException("Input string cannot be null or empty");
        
        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int index = c < 128 ? charIndex[c] : -1;
            if (index == -1) throw new IllegalArgumentException("Invalid character in Base36 string: " + c);
            
            long newResult = result * chars.length + index;
            if (newResult < result) throw new IllegalArgumentException("Number overflow during Base36 decode");
            result = newResult;
        }
        
        return result;
    }
    
}
