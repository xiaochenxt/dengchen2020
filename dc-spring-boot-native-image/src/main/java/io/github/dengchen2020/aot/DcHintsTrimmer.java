package io.github.dengchen2020.aot;

import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * 大部分用户用不到@Value绑定MediaType却为此引入AWT，所以这里移除对应的反射注册。（必须使用@ImportRuntimeHints注册而不是aot.factories）
 *
 * @author xiaochen
 * @since 2026/11/28
 */
public final class DcHintsTrimmer implements RuntimeHintsRegistrar {

    private static final String KEY = "dc.native.trim-property-editor-hints";

    @SuppressWarnings("unchecked")
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        if (!isEnabled(classLoader)) {
            return;
        }
        try {
            var typesField = ReflectionHints.class.getDeclaredField("types");
            typesField.setAccessible(true);
            Map<TypeReference, ?> types = (Map<TypeReference, ?>) typesField.get(hints.reflection());
            types.remove(TypeReference.of("org.springframework.core.io.ResourceEditor"));
            types.remove(TypeReference.of("org.springframework.http.MediaTypeEditor"));
        } catch (Exception ignored) {
            // 内部字段结构变化时静默降级，不影响构建
        }
    }

    private static boolean isEnabled(ClassLoader classLoader) {
        var value = System.getProperty(KEY);
        if (value != null) return Boolean.parseBoolean(value);
        var loader = (classLoader != null) ? classLoader : DcHintsTrimmer.class.getClassLoader();
        var properties = new Properties();
        try (var in = loader.getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException ignored) {
            // 读不到就用默认值
        }
        return Boolean.parseBoolean(properties.getProperty(KEY, "true"));
    }
}
