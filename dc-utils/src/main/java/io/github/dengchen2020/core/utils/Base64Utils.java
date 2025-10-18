package io.github.dengchen2020.core.utils;

import java.util.Base64;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_BYTE_ARRAY;
import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING;

/**
 * Base64编解码工具类，详见{@link Base64}
 * <pre>
 * 主要三种编解码器
 * 标准版：适用于以下场景之外
 * Url：适用于浏览器上的Url
 * MIME：适用于图片、音频、视频、文件等
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
        return src.length == 0 ? EMPTY_BYTE_ARRAY : Base64.getDecoder().decode(src);
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
        return new String(encode(src));
    }

    public static String encodeUrlToString(String src) {
        return src == null ? EMPTY_STRING : encodeUrlToString(src.getBytes());
    }

    public static byte[] decodeUrl(byte[] src) {
        return src.length == 0 ? EMPTY_BYTE_ARRAY : Base64.getUrlDecoder().decode(src);
    }

    public static byte[] decodeUrl(String src) {
        return src == null ? EMPTY_BYTE_ARRAY : decodeUrl(src.getBytes());
    }

    public static String decodeUrlToString(byte[] src) {
        return new String(decodeUrl(src));
    }

    public static String decodeUrlToString(String src) {
        return src == null ? EMPTY_STRING : decodeUrlToString(src.getBytes());
    }

    public static byte[] encodeMime(byte[] src) {
        return src.length == 0 ? EMPTY_BYTE_ARRAY : Base64.getMimeEncoder().encode(src);
    }

    public static byte[] encodeMime(String src) {
        return src == null ? EMPTY_BYTE_ARRAY : encodeUrl(src.getBytes());
    }

    public static String encodeMimeToString(byte[] src) {
        return new String(encode(src));
    }

    public static String encodeMimeToString(String src) {
        return src == null ? EMPTY_STRING : encodeMimeToString(src.getBytes());
    }

    public static byte[] decodeMime(byte[] src) {
        return src.length == 0 ? EMPTY_BYTE_ARRAY : Base64.getMimeDecoder().decode(src);
    }

    public static byte[] decodeMime(String src) {
        return src == null ? EMPTY_BYTE_ARRAY : decodeUrl(src.getBytes());
    }

    public static String decodeMimeToString(byte[] src) {
        return new String(decodeMime(src));
    }

    public static String decodeMimeToString(String src) {
        return src == null ? EMPTY_STRING : decodeMimeToString(src.getBytes());
    }

}
