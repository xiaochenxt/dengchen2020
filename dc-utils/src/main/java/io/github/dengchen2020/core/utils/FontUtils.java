package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;

import java.awt.*;

/**
 * 字体工具类
 * @author xiaochen
 * @since 2026/11/9
 */
@NullMarked
public abstract class FontUtils {

    public static final Font CHINESE_FONT = new Font("宋体", Font.BOLD, 50);

    public static Font chineseFont() {
        return CHINESE_FONT;
    }

    public static Font chineseFont(int style, float size) {
        return CHINESE_FONT.deriveFont(style, size);
    }

}
