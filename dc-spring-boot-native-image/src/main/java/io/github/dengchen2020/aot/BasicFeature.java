package io.github.dengchen2020.aot;

import io.github.dengchen2020.aot.utils.FeatureUtils;
import org.graalvm.nativeimage.hosted.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Set;

import static io.github.dengchen2020.aot.utils.CollectUtils.EMPTY_CLASS_ARRAY;
import static io.github.dengchen2020.aot.utils.CollectUtils.debug;

/**
 * 基本注册，解决了一些代理检测无法自动配置的场景
 * <p>
 * 可搭配代理检测自动收集配置，但需注意，尽量不要在idea中启动，如果在idea等开发工具中启动，会收集idea的agent，
 * 会多出sun.instrument.InstrumentationImpl和com.intellij.rt.execution.application.AppMainV2$Agent等
 * ，最好移除掉（搜索agent、intellij等）</p>
 * <p>虚拟机选项：-agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image</p>
 * <p>代理使用方式：java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image -jar app.jar</p>
 *
 * @author xiaochen
 * @since 2025/8/20
 */
class BasicFeature implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        FeatureUtils featureUtils = new FeatureUtils(access.getApplicationClassLoader());
        caffeine(featureUtils, access);
        lettuce(featureUtils);
        if (Boolean.parseBoolean(System.getProperty("font.aot","true"))) font(featureUtils, access);
        aliyuncs(featureUtils, access);
        captcha(featureUtils, access);
        phonenumbers(featureUtils, access);
        serializedLambda(featureUtils, access);
        graalJs(featureUtils, access);
        jetty(featureUtils, access);
        image(featureUtils, access);
        dns(featureUtils, access);
        ssl(featureUtils, access);
        precompute(featureUtils, access);
        playwright(featureUtils, access);
    }

    /**
     * 要解决windows环境下的字体问题，需要有java.home，java.home所在文件夹下有lib文件夹，
     * lib文件夹中要有fontconfig.bfc、fontconfig.properties.src、psfont.properties.ja、psfontj2d.properties
     */
    private void font(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        access.registerReachabilityHandler(duringAnalysisAccess -> {
            try {
                new FontRequiredRegister().register(new FeatureUtils(duringAnalysisAccess.getApplicationClassLoader()));
            } catch (Exception e) {
                System.out.println("字体注册异常，可能导致字体相关功能无法使用");
                e.printStackTrace();
            }
            if (Runtime.version().feature() >= 19) {
                Class<?> fontUtilitiesClass = featureUtils.loadClass("sun.font.FontUtilities");
                if (fontUtilitiesClass != null) {
                    RuntimeJNIAccess.register(fontUtilitiesClass);
                    RuntimeJNIAccess.register(fontUtilitiesClass.getDeclaredFields());
                    RuntimeJNIAccess.register(fontUtilitiesClass.getDeclaredMethods());
                    RuntimeJNIAccess.register(fontUtilitiesClass.getDeclaredConstructors());
                }
            }
            FeatureUtils fe = new FeatureUtils(duringAnalysisAccess.getApplicationClassLoader());
            fe.registerJniIfPresent("sun.awt.windows.WComponentPeer","sun.awt.windows.WDesktopPeer",
                    "sun.awt.windows.WObjectPeer","sun.awt.windows.WToolkit","sun.java2d.windows.WindowsFlags");
            fe.registerJniIfPresent("java.awt.Toolkit","java.awt.Insets","java.awt.FontMetrics","java.awt.Font",
                    "sun.awt.image.SunVolatileImage","sun.awt.image.VolatileSurfaceManager","java.awt.Component",
                    "java.awt.desktop.UserSessionEvent$Reason","sun.awt.Win32GraphicsEnvironment");

            String javaHome = System.getProperty("java.home");
            URL url = fe.classLoader().getResource("");
            if (url != null) {
                String path = url.getPath();
                String targetPath = new File(path).getParentFile().getPath();
                File f = new File(targetPath + File.separator + "lib");
                if (f.mkdirs()) {
                    String libPath = javaHome + File.separator + "lib" + File.separator;
                    File fontconfig = new File(libPath + "fontconfig.bfc");
                    File fontPropertiiesSrc = new File(libPath + "fontconfig.properties.src");
                    File psfontPropertiesJa = new File(libPath + "psfont.properties.ja");
                    File psfontj2dProperties = new File(libPath + "psfontj2d.properties");
                    String targetLibPath = targetPath + File.separator + "lib" + File.separator;
                    if (fontconfig.exists()) {
                        try {
                            Files.copy(fontconfig.toPath(), Path.of(targetLibPath + "fontconfig.bfc"));
                        } catch (IOException ignored) {}
                    }
                    if (fontPropertiiesSrc.exists()) {
                        try {
                            Files.copy(fontPropertiiesSrc.toPath(), Path.of(targetLibPath + "fontconfig.properties.src"));
                        } catch (IOException ignored) {}
                    }
                    if (psfontPropertiesJa.exists()) {
                        try {
                            Files.copy(psfontPropertiesJa.toPath(), Path.of(targetLibPath + "psfont.properties.ja"));
                        } catch (IOException ignored) {}
                    }
                    if (psfontj2dProperties.exists()) {
                        try {
                            Files.copy(psfontj2dProperties.toPath(), Path.of(targetLibPath + "psfontj2d.properties"));
                        } catch (IOException ignored) {}
                    }
                }
            }
            // 需将运行时的java.home设置为当前目录
            fe.registerSystemProperty("java.home", "./");
            System.out.println("字体依赖lib文件夹，需要带上它一起打包");
        }, Font.class);
    }

    /**
     * caffine的基本反射注册，正常情况下足够了
     * @param featureUtils
     */
    private void caffeine(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> nodeFactory = featureUtils.loadClass("com.github.benmanes.caffeine.cache.NodeFactory");
        if (nodeFactory != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                featureUtils.registerReflectionConstructorsIfPresent(
                        "com.github.benmanes.caffeine.cache.PD","com.github.benmanes.caffeine.cache.PDA","com.github.benmanes.caffeine.cache.PDAMS",
                        "com.github.benmanes.caffeine.cache.PDW", "com.github.benmanes.caffeine.cache.PDWMS","com.github.benmanes.caffeine.cache.PS",
                        "com.github.benmanes.caffeine.cache.PSA","com.github.benmanes.caffeine.cache.PSAMS","com.github.benmanes.caffeine.cache.PSW",
                        "com.github.benmanes.caffeine.cache.PSWMS");
            }, nodeFactory);
        }
        Class<?> localCacheFactory = featureUtils.loadClass("com.github.benmanes.caffeine.cache.LocalCacheFactory");
        if (localCacheFactory != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                featureUtils.registerReflectionConstructorsIfPresent(
                        "com.github.benmanes.caffeine.cache.SIMSA","com.github.benmanes.caffeine.cache.SIMSW",
                        "com.github.benmanes.caffeine.cache.SSMSA","com.github.benmanes.caffeine.cache.SSMSW");
            }, localCacheFactory);
        }
    }

    private void lettuce(FeatureUtils featureUtils) {
        if (featureUtils.isPresent("io.lettuce.core.RedisClient")) RuntimeSystemProperties.register("io.lettuce.core.jfr", "false");
    }

    private void aliyuncs(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        if (featureUtils.isPresent("com.aliyuncs.http.HttpClientFactory")) {
            Class<?> apacheHttpClient = featureUtils.loadClass("com.aliyuncs.http.clients.ApacheHttpClient");
            if (apacheHttpClient != null) {
                access.registerReachabilityHandler(duringAnalysisAccess -> {
                    try {
                        RuntimeReflection.register(apacheHttpClient);
                        featureUtils.registerResource(apacheHttpClient,"endpoints.json");
                        Class<?> assumeRoleResponse = featureUtils.loadClass("com.aliyuncs.auth.sts.AssumeRoleResponse");
                        if (assumeRoleResponse != null) {
                            featureUtils.registerReflection(assumeRoleResponse);
                            RuntimeReflection.registerForReflectiveInstantiation(featureUtils.classLoader().loadClass("com.aliyuncs.auth.sts.AssumeRoleResponse"));
                            featureUtils.registerReflection(assumeRoleResponse.getClasses());
                        }
                    } catch (Exception ignored) {}
                }, apacheHttpClient);
            }
        }
    }

    private void captcha(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> captcha = featureUtils.loadClass("com.wf.captcha.base.Captcha");
        if (captcha != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                // 仅添加第一个字体，需要其他的自行添加
                featureUtils.registerResource(captcha,"epilog.ttf");
            }, captcha);
        }
    }

    private void phonenumbers(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> phoneNumberUtil =  featureUtils.loadClass("com.google.i18n.phonenumbers.PhoneNumberUtil");
        if (phoneNumberUtil != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                // 这里仅添加中国大陆、中国香港、中国澳门、中国台湾、俄罗斯、新加坡、美国、韩国的手机号元数据，需要其他的自行添加
                featureUtils.registerResource(phoneNumberUtil,"com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_CN",
                        "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_HK",
                        "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_MO",
                        "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_TW",
                        "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_RU",
                        "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_SG",
                        "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_US",
                        "com/google/i18n/phonenumbers/data/PhoneNumberMetadataProto_KR");
            },  phoneNumberUtil);
        }
    }

    private void serializedLambda(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        access.registerReachabilityHandler(duringAnalysisAccess -> {
            RuntimeSerialization.register(SerializedLambda.class);
            try {
                Set<Class<?>> classes = featureUtils.collectClass(featureUtils.findMainPackages());
                classes.forEach(featureUtils::registerSerializationLambdaCapturingClass);
            } catch (Exception ignored) {}
        }, SerializedLambda.class);
    }

    private void graalJs(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> graalJs = featureUtils.loadClass("com.oracle.truffle.js.scriptengine.GraalJSScriptEngine");
        if (graalJs != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                // 抑制 -Dtruffle.UseFallbackRuntime=true 带来的因为未启用运行时优化导致性能降低的警告
                featureUtils.registerSystemProperty("polyglot.engine.WarnInterpreterOnly", "false");
            }, graalJs);
        }
    }

    private void jetty(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> jettyWebSocket = featureUtils.loadClass("org.eclipse.jetty.ee10.websocket.jakarta.server.JakartaWebSocketServerContainer");
        if (jettyWebSocket != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                try {
                    new JettyWebSocketRequiredRegister().register(featureUtils);
                } catch (Exception ignored) {}
            }, jettyWebSocket);
        }
    }

    private void image(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> imageReader = featureUtils.loadClass("com.sun.imageio.plugins.jpeg.JPEGImageReader");
        if (imageReader != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                featureUtils.registerJniMethods(imageReader, "acceptPixels","passComplete","passStarted","pushBack","readInputData","setImageData","skipInputBytes","skipPastImage","warningOccurred","warningWithMessage");
                featureUtils.registerJniFields("javax.imageio.plugins.jpeg.JPEGQTable","qTable");
                featureUtils.registerJniFields("javax.imageio.plugins.jpeg.JPEGHuffmanTable","lengths","values");
                featureUtils.registerJniFields("sun.awt.image.ByteComponentRaster","data","dataOffsets","pixelStride","scanlineStride","type");
                featureUtils.registerJniMethods("java.util.HashMap", "containsKey","put");
                featureUtils.registerJni(ArrayList.class.getDeclaredConstructors());
                featureUtils.registerJniMethods("java.util.ArrayList", "add");
                featureUtils.registerJniMethods("java.lang.String", "toLowerCase");
                featureUtils.registerJniMethods("com.sun.imageio.plugins.jpeg.JPEGImageWriter", "grabPixels","warningOccurred","warningWithMessage","writeMetadata","writeOutputData");
            }, imageReader);
        }
    }

    /**
     * redisson需要
     */
    private void dns(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> resolverConfigurationImpl = featureUtils.loadClass("sun.net.dns.ResolverConfigurationImpl");
        if (resolverConfigurationImpl != null) {
            RuntimeClassInitialization.initializeAtRunTime(resolverConfigurationImpl);
            featureUtils.registerJniFields(resolverConfigurationImpl,"os_searchlist","os_nameservers");
        }
    }

    private void ssl(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> sslSocketFactory = featureUtils.loadClass("javax.net.ssl.SSLSocketFactory");
        if (sslSocketFactory != null) {
            try {
                Method getDefault = sslSocketFactory.getDeclaredMethod("getDefault");
                access.registerReachabilityHandler(duringAnalysisAccess -> {
                    RuntimeReflection.register(sslSocketFactory);
                    RuntimeReflection.register(getDefault);
                }, getDefault);
            } catch (NoSuchMethodException ignored) {}
        }
    }

    /**
     * 将与某些模式匹配的字段值替换为提前计算好的值，而不会导致类构建时初始化
     */
    private void precompute(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> webSocketClientUtils = featureUtils.loadClass("io.github.dengchen2020.websocket.client.WebSocketClientUtils");
        if (webSocketClientUtils != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                for (Field field : featureUtils.collectFields(webSocketClientUtils, "jakartaWebSocketClientContainerProviderPresent", "wsWebSocketContainerPresent")) {
                    try {
                        field.setAccessible(true);
                        Object fieldValue = field.get(null);
                        access.registerFieldValueTransformer(field, (receiver, originalValue) -> fieldValue);
                        if (debug) System.out.println("Field " + webSocketClientUtils.getName() + "#" + field.getName() + " set to " + fieldValue + " at build time");
                    } catch (IllegalAccessException ignored) {}
                }
            },  webSocketClientUtils);
        }
    }

    /**
     * 用于驱动浏览器实现自动化测试
     */
    private void playwright(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> playwright = featureUtils.loadClass("com.microsoft.playwright.Playwright");
        if (playwright == null) return;
        access.registerReachabilityHandler(duringAnalysisAccess -> {
            try {
                String dir = "playwright";
                featureUtils.registerSystemProperty("playwright.cli.dir", dir);
                featureUtils.registerReflection(featureUtils.collectClass("com.microsoft.playwright").toArray(EMPTY_CLASS_ARRAY));
                featureUtils.registerJniIfPresent("java.awt.event.InputEvent","java.awt.AWTEvent","sun.awt.Win32GraphicsDevice","java.awt.image.IndexColorModel","sun.awt.SunToolkit","sun.awt.AWTAutoShutdown","sun.java2d.d3d.D3DRenderQueue","sun.java2d.d3d.D3DRenderQueue$1",
                        "sun.java2d.d3d.D3DGraphicsDevice","sun.java2d.d3d.D3DGraphicsDevice$1");
                featureUtils.registerReflectionIfPresent("sun.awt.Win32GraphicsDevice");
                // 需要将驱动文件放入resources中的playwright目录下，默认会生成驱动文件到C:\Users\用户目录\AppData\Local\Temp\playwright-java-{随机数}文件夹中，可以复制它
                URL url = featureUtils.classLoader().getResource(dir);
                if (url != null) {
                    String playwrightPath = url.getPath();
                    Path sourceDirectory = new File(playwrightPath).toPath();
                    String targetPath = new File(playwrightPath).getParentFile().getParentFile().getPath();
                    Path targetDirectory = Paths.get(targetPath + File.separator + dir);
                    try (var stream = Files.walk(sourceDirectory)) {
                        stream.forEach(source -> {
                            Path target = targetDirectory.resolve(sourceDirectory.relativize(source));
                            try {
                                if (Files.isDirectory(source)) {
                                    Files.createDirectories(target);
                                } else {
                                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } catch (Exception ignored) {

            }
        }, playwright);
    }

}
