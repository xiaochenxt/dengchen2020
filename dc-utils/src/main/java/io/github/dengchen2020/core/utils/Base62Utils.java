package io.github.dengchen2020.core.utils;

import java.util.Arrays;

/**
 * Base62编解码工具类
 * <pre>
 * Base62编码使用62个字符：0-9, a-z, A-Z
 * 适用于生成短URL、短ID等场景
 * </pre>
 * @author xiaochen
 * @since 2025/12/13
 */
public abstract class Base62Utils {
    
    private static final char[] CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int[] CHAR_INDEX = new int[128];
    
    static {
        Arrays.fill(CHAR_INDEX, -1);
        for (int i = 0; i < CHARS.length; i++) {
            CHAR_INDEX[CHARS[i]] = i;
        }
    }
    
    /**
     * 将long类型数字编码为Base62字符串
     * @param num 待编码的数字（必须为非负数）
     * @return Base62编码字符串
     */
    public static String encode(long num) {
        if (num < 0) throw new IllegalArgumentException("Input number must be non-negative: " + num);
        if (num == 0) return "0";
        
        char[] buf = new char[11];
        int pos = buf.length;
        
        while (num > 0) {
            buf[--pos] = CHARS[(int) (num % CHARS.length)];
            num /= CHARS.length;
        }
        
        return new String(buf, pos, buf.length - pos);
    }
    
    /**
     * 将Base62字符串解码为long类型数字
     * @param str Base62编码的字符串
     * @return 解码后的数字
     * @throws IllegalArgumentException 如果字符串包含非法字符
     */
    public static long decode(String str) {
        if (str == null || str.isEmpty()) throw new IllegalArgumentException("Input string cannot be null or empty");
        
        long result = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int index = c < 128 ? CHAR_INDEX[c] : -1;
            if (index == -1) throw new IllegalArgumentException("Invalid character in Base62 string: " + c);
            
            long newResult = result * CHARS.length + index;
            if (newResult < result) throw new IllegalArgumentException("Number overflow during Base62 decode");
            result = newResult;
        }
        
        return result;
    }
    
}
