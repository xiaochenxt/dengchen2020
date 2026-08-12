package io.github.dengchen2020.core.utils;

import java.util.Base64;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_BYTE_ARRAY;
import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING;

/**
 * Base64编解码工具类，详见{@link Base64}
 * <pre>
 * 主要三种编解码器
 * 标准版：适用于以下场景之外，例如图片编码
 * Url：适用于Url参数、header（例如JWT）等
 * </pre>
 * @author xiaochen
 * @since 2024/12/29
 */
public abstract class Base64Utils {
    
    public static byte[] encode(byte[] src) {
        return src.length == 0 ? EMPTY_BYTE_ARRAY : Base64.getEncoder().encode(src);
    }
    
    public static byte[] encode(String src) {
        return src == null ? EMPTY_BYTE_ARRAY : encode(src.getBytes());
    }

    public static String encodeToString(byte[] src) {
        return new String(encode(src));
    }
    
    public static String encodeToString(String src) {
        return src == null ? EMPTY_STRING : encodeToString(src.getBytes());
    }

    public static byte[] decode(byte[] src) {
        if (src.length == 0) return EMPTY_BYTE_ARRAY;
        for (byte b : src) {
            if (b == '-' || b == '_') return Base64.getUrlDecoder().decode(src);
        }
        return Base64.getDecoder().decode(src);
    }

    public static byte[] decode(String src) {
        return src == null ? EMPTY_BYTE_ARRAY : decode(src.getBytes());
    }

    public static String decodeToString(byte[] src) {
        return new String(decode(src));
    }
    
    public static String decodeToString(String src) {
        return src == null ? EMPTY_STRING : decodeToString(src.getBytes());
    }

    public static byte[] encodeUrl(byte[] src) {
        return src.length == 0 ? EMPTY_BYTE_ARRAY : Base64.getUrlEncoder().encode(src);
    }

    public static byte[] encodeUrl(String src) {
        return src == null ? EMPTY_BYTE_ARRAY : encodeUrl(src.getBytes());
    }

    public static String encodeUrlToString(byte[] src) {
        return new String(encodeUrl(src));
    }

    public static String encodeUrlToString(String src) {
        return src == null ? EMPTY_STRING : encodeUrlToString(src.getBytes());
    }

}
