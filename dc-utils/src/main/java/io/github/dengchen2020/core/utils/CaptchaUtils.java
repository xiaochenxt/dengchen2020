package io.github.dengchen2020.core.utils;

import com.wf.captcha.*;
import com.wf.captcha.base.Captcha;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 验证码工具类
 *
 * @author xiaochen
 * @since 2024/4/15
 */
public abstract class CaptchaUtils {

    private static final int width = 200;

    private static final int height = 72;

    private static final int len = 4;

    private static final float fontSize = 50f;

    /**
     * 验证码
     *
     * @param type 1-普通验证码 2-算数验证码 3-汉字验证码 4-汉字动图验证码 5-动图验证码
     * @return 验证码
     */
    public static Captcha captcha(int type) {
        return captcha(type, len);
    }

    /**
     * 验证码
     *
     * @param type 1-普通验证码 2-算数验证码 3-汉字验证码 4-汉字动图验证码 5-动图验证码
     * @param len  验证码内容长度
     * @return 验证码
     */
    public static Captcha captcha(int type, int len) {
        return captcha(type, width, height, len, fontSize);
    }

    /**
     * 验证码
     *
     * @param out  输出流
     * @param type 1-普通验证码 2-算数验证码 3-汉字验证码 4-汉字动图验证码 5-动图验证码
     * @return 验证码答案
     */
    public static String captcha(OutputStream out, int type) {
        return captcha(out, type, width, height, len, fontSize);
    }

    /**
     * 验证码
     *
     * @param out      输出流
     * @param type     1-普通验证码 2-算数验证码 3-汉字验证码 4-汉字动图验证码 5-动图验证码
     * @param width    验证码图宽度
     * @param height   验证码图高度
     * @param len      验证码内容长度
     * @param fontSize 验证码内容文字大小
     * @return 验证码答案
     */
    public static String captcha(OutputStream out, int type, int width, int height, int len, float fontSize) {
        Captcha captcha = captcha(type, width, height, len, fontSize);
        captcha.out(out);
        return captcha.text();
    }

    /**
     * 验证码
     *
     * @param type     1-普通验证码 2-算数验证码 3-汉字验证码 4-汉字动图验证码 5-动图验证码
     * @param width    验证码图宽度
     * @param height   验证码图高度
     * @param len      验证码内容长度
     * @param fontSize 验证码内容文字大小
     * @return 验证码
     */
    public static Captcha captcha(int type, int width, int height, int len, float fontSize) {
        return switch (type) {
            case 2 -> arithmetic(width, height, len, fontSize);
            case 3 -> chinese(width, height, len, fontSize);
            case 4 -> chineseGif(width, height, len, fontSize);
            case 5 -> gif(width, height, len, fontSize);
            default -> spec(width, height, len, fontSize);
        };
    }

    /**
     * 普通验证码
     *
     * @return 验证码
     */
    public static SpecCaptcha spec() {
        return spec(width, height, len, fontSize);
    }

    /**
     * 普通验证码
     *
     * @param width    验证码图宽度
     * @param height   验证码图高度
     * @param len      验证码内容长度
     * @param fontSize 验证码内容文字大小
     * @return 验证码
     */
    public static SpecCaptcha spec(int width, int height, int len, float fontSize) {
        SpecCaptcha specCaptcha = new SpecCaptcha(width, height, len);
        try {
            specCaptcha.setFont(1, fontSize);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("生成验证码失败", e);
        }
        return specCaptcha;
    }

    /**
     * 算术验证码
     *
     * @return 验证码
     */
    public static ArithmeticCaptcha arithmetic() {
        return arithmetic(width, height, len, fontSize);
    }

    /**
     * 算术验证码
     *
     * @param width    验证码图宽度
     * @param height   验证码图高度
     * @param len      验证码内容长度
     * @param fontSize 验证码内容文字大小
     * @return 验证码
     */
    public static ArithmeticCaptcha arithmetic(int width, int height, int len, float fontSize) {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(width, height, len);
        try {
            captcha.setFont(1, fontSize);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("生成数字验证码失败", e);
        }
        return captcha;
    }

    /**
     * 汉字验证码
     *
     * @return 验证码
     */
    public static ChineseCaptcha chinese() {
        return chinese(width, height, len, fontSize);
    }

    /**
     * 汉字验证码
     *
     * @param width    验证码图宽度
     * @param height   验证码图高度
     * @param len      验证码内容长度
     * @param fontSize 验证码内容文字大小
     * @return 验证码
     */
    public static ChineseCaptcha chinese(int width, int height, int len, float fontSize) {
        ChineseCaptcha captcha = new ChineseCaptcha(width, height, len);
        captcha.setFont(new Font("宋体", Font.BOLD, (int) fontSize));
        return captcha;
    }

    /**
     * 汉字动图验证码
     *
     * @return 验证码
     */
    public static ChineseGifCaptcha chineseGif() {
        return chineseGif(width, height, len, fontSize);
    }

    /**
     * 汉字动图验证码
     *
     * @param width    验证码图宽度
     * @param height   验证码图高度
     * @param len      验证码内容长度
     * @param fontSize 验证码内容文字大小
     * @return 验证码
     */
    public static ChineseGifCaptcha chineseGif(int width, int height, int len, float fontSize) {
        ChineseGifCaptcha captcha = new ChineseGifCaptcha(width, height, len);
        captcha.setFont(new Font("宋体", Font.BOLD, (int) fontSize));
        return captcha;
    }

    /**
     * 动图验证码
     *
     * @return 验证码
     */
    public static GifCaptcha gif() {
        return gif(width, height, len, fontSize);
    }

    /**
     * 动图验证码
     *
     * @param width    验证码图宽度
     * @param height   验证码图高度
     * @param len      验证码内容长度
     * @param fontSize 验证码内容文字大小
     * @return 验证码
     */
    public static GifCaptcha gif(int width, int height, int len, float fontSize) {
        GifCaptcha captcha = new GifCaptcha(width, height, len);
        try {
            captcha.setFont(1, fontSize);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("生成验证码失败", e);
        }
        return captcha;
    }

}
