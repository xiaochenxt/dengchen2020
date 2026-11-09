package io.github.dengchen2020.aot;

import org.graalvm.nativeimage.hosted.Feature;

import java.lang.reflect.Method;
import java.nio.charset.Charset;

/**
 * 在 native-image 包含的默认字符集之外，精确追加 GBK 系列编码，
 * 避免 -H:+AddAllCharsets 把全部字符集打进镜像带来的体积膨胀。
 */
public class GbkCharsetFeature implements Feature {

    @Override
    public void afterRegistration(AfterRegistrationAccess access) {
        try {
            if (isAddAllCharsetsEnabled()) {
                // -H:+AddAllCharsets 已在构建期全量注册所有字符集，这里无需重复添加
                return;
            }
            // com.oracle.svm.hosted.jdk.localization.LocalizationFeature#addCharset(Charset) 是 public static，
            // 它会同时注册字符集别名，并触发编解码器类的可达性分析。
            Class<?> localizationFeature = Class.forName("com.oracle.svm.hosted.jdk.localization.LocalizationFeature");
            Method addCharset = localizationFeature.getMethod("addCharset", Charset.class);
            final String[] charsets = {"GB18030","GBK","GB2312"};
            for (String name : charsets) {
                addCharset.invoke(null, Charset.forName(name));
            }
        } catch (ReflectiveOperationException e) {
            System.err.println("Failed to add GBK charsets to the native image: " + e.getMessage());
        }
    }

    /**
     * 读取 -H:AddAllCharsets 开关，已全量注册时跳过重复添加。
     */
    private static boolean isAddAllCharsetsEnabled() throws ReflectiveOperationException {
        Class<?> options = Class.forName("com.oracle.svm.hosted.jdk.localization.LocalizationFeature$Options");
        Object key = options.getField("AddAllCharsets").get(null);
        return (Boolean) key.getClass().getMethod("getValue").invoke(key);
    }
}
