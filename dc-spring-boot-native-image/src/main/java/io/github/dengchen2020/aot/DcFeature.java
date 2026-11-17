package io.github.dengchen2020.aot;

import io.github.dengchen2020.aot.utils.FeatureUtils;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.RuntimeSerialization;

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
import java.util.Locale;
import java.util.Set;

import static io.github.dengchen2020.aot.utils.CollectUtils.*;

/**
 * 基本注册，解决了一些配置收集困难的场景
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
class DcFeature implements Feature {

    @Override
    public String getDescription() {
        return "Resolved some scenarios where collecting configurations was difficult";
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        FeatureUtils featureUtils = new FeatureUtils(access.getApplicationClassLoader());
        awt(featureUtils, access);
        lettuce(featureUtils);
        serializedLambda(featureUtils, access);
        graalJs(featureUtils, access);
        jetty(featureUtils, access);
        dns(featureUtils, access);
        ssl(featureUtils, access);
        precompute(featureUtils, access);
        playwright(featureUtils, access);
        ognl(featureUtils, access);
    }

    private void awt(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        access.registerReachabilityHandler(duringAnalysisAccess -> {
            featureUtils.registerJniIfPresent("java.awt.AWTEvent","java.awt.AlphaComposite","java.awt.Color",
                    "java.awt.GraphicsEnvironment","java.awt.Component","java.awt.Font","java.awt.FontMetrics",
                    "java.awt.Insets","java.awt.Toolkit","java.awt.desktop.UserSessionEvent$Reason",
                    "java.awt.event.InputEvent","java.awt.geom.AffineTransform","java.awt.geom.GeneralPath",
                    "java.awt.geom.Path2D","java.awt.geom.Path2D$Float","java.awt.geom.Point2D$Float",
                    "java.awt.geom.Rectangle2D$Float","java.awt.image.BufferedImage","java.awt.image.ColorModel",
                    "java.awt.image.IndexColorModel","java.awt.image.Raster","java.awt.image.SampleModel",
                    "java.awt.image.SinglePixelPackedSampleModel","java.lang.Boolean","sun.awt.AWTAutoShutdown",
                    "sun.awt.SunHints","sun.awt.SunToolkit","sun.awt.Win32GraphicsConfig",
                    "sun.awt.Win32GraphicsDevice","sun.awt.Win32GraphicsEnvironment",
                    "sun.awt.image.BufImgSurfaceData$ICMColorData","sun.awt.image.IntegerComponentRaster",
                    "sun.awt.image.SunVolatileImage","sun.awt.image.VolatileSurfaceManager",
                    "sun.awt.windows.WComponentPeer","sun.awt.windows.WDesktopPeer","sun.awt.windows.WObjectPeer",
                    "sun.awt.windows.WToolkit","sun.font.CharToGlyphMapper","sun.font.Font2D",
                    "sun.font.FontConfigManager$FcCompFont","sun.font.FontConfigManager$FontConfigFont",
                    "sun.font.FontConfigManager$FontConfigInfo","sun.font.FontStrike","sun.font.FontUtilities",
                    "sun.font.FreetypeFontScaler","sun.font.GlyphList","sun.font.PhysicalStrike",
                    "sun.font.StrikeCache$1","sun.font.StrikeMetrics","sun.font.TrueTypeFont",
                    "sun.font.Type1Font","sun.java2d.Disposer","sun.java2d.InvalidPipeException",
                    "sun.java2d.NullSurfaceData","sun.java2d.SunGraphics2D","sun.java2d.SurfaceData",
                    "sun.java2d.d3d.D3DGraphicsDevice", "sun.java2d.d3d.D3DGraphicsDevice$1","sun.java2d.d3d.D3DRenderQueue$1",
                    "sun.java2d.loops.Blit", "sun.java2d.loops.BlitBg","sun.java2d.loops.CompositeType","sun.java2d.loops.DrawGlyphList",
                    "sun.java2d.loops.DrawGlyphListAA","sun.java2d.loops.DrawGlyphListLCD",
                    "sun.java2d.loops.DrawLine","sun.java2d.loops.DrawParallelogram","sun.java2d.loops.DrawPath",
                    "sun.java2d.loops.DrawPolygons","sun.java2d.loops.DrawRect","sun.java2d.loops.FillParallelogram",
                    "sun.java2d.loops.FillPath","sun.java2d.loops.FillRect","sun.java2d.loops.FillSpans",
                    "sun.java2d.loops.GraphicsPrimitive","sun.java2d.loops.GraphicsPrimitiveMgr",
                    "sun.java2d.loops.MaskBlit","sun.java2d.loops.MaskFill","sun.java2d.loops.ScaledBlit",
                    "sun.java2d.loops.SurfaceType","sun.java2d.loops.TransformHelper","sun.java2d.loops.XORComposite",
                    "sun.java2d.pipe.Region","sun.java2d.pipe.RegionIterator","sun.java2d.windows.WindowsFlags",
                    "com.sun.imageio.plugins.jpeg.JPEGImageReader","com.sun.imageio.plugins.jpeg.JPEGImageWriter",
                    "java.awt.Dimension","javax.imageio.plugins.jpeg.JPEGQTable","javax.imageio.plugins.jpeg.JPEGHuffmanTable","sun.awt.image.ByteComponentRaster",
                    "javax.imageio.IIOException","java.util.HashMap","java.util.ArrayList","java.lang.String");
            featureUtils.registerReflectionIfPresent("javax.imageio.spi.ImageReaderWriterSpi",
                    "javax.imageio.spi.ImageReaderSpi",
                    "javax.imageio.spi.ImageWriterSpi","sun.font.FontConfigManager$FontConfigFont[]",
                    "sun.java2d.loops.GraphicsPrimitive[]","sun.java2d.marlin.DMarlinRenderingEngine",
                    "sun.security.provider.DRBG","sun.security.provider.NativePRNG","sun.security.provider.SHA",
                    "sun.security.provider.SHA2$SHA256","sun.util.resources.cldr.TimeZoneNames",
                    "sun.util.resources.cldr.TimeZoneNames_en","sun.util.resources.cldr.TimeZoneNames_en_US");
            featureUtils.registerResource(Font.class, "META-INF/services/javax.imageio.spi.ImageInputStreamSpi",
                    "META-INF/services/javax.imageio.spi.ImageOutputStreamSpi",
                    "META-INF/services/javax.imageio.spi.ImageReaderSpi",
                    "META-INF/services/javax.imageio.spi.ImageTranscoderSpi",
                    "META-INF/services/javax.imageio.spi.ImageWriterSpi",
                    "sun/awt/resources/awt_en.properties",
                    "sun/awt/resources/awt_en_US.properties",
                    "sun/awt/resources/awt_zh.properties",
                    "sun/awt/resources/awt_zh_Hans.properties",
                    "sun/awt/resources/awt_zh_Hans_CN.properties");
            featureUtils.registerResourceBundle(Font.class, "sun.awt.resources.awt", Locale.getDefault(), Locale.ENGLISH, Locale.CHINESE);

        }, java.awt.Font.class,java.awt.Image.class, java.awt.AWTEvent.class);
    }

    private void lettuce(FeatureUtils featureUtils) {
        if (featureUtils.isPresent("io.lettuce.core.RedisClient")) featureUtils.registerSystemProperty("io.lettuce.core.jfr", "false");
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
        Class<?> jettyWebSocket = featureUtils.loadClass("org.eclipse.jetty.ee11.websocket.jakarta.server.JakartaWebSocketServerContainer");
        if (jettyWebSocket != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                try {
                    new JettyWebSocketRequiredRegister().register(featureUtils);
                } catch (Exception ignored) {}
            }, jettyWebSocket);
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

    /**
     * 解决ognl3.5.0以下版本的性能问题 </br>
     * 禁用ognl的securityManager（3.5.0移除了securityManager），默认情况下也没启用，但是会有大量无效的系统属性空查询调用，导致线程阻塞而产生性能问题
     */
    private void ognl(FeatureUtils featureUtils, BeforeAnalysisAccess access) {
        Class<?> mybatisOgnl = featureUtils.loadClass("org.apache.ibatis.ognl.security.OgnlSecurityManagerFactory");
        if(mybatisOgnl != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                // 详见：org.apache.ibatis.ognl.OgnlRuntime 881行
                featureUtils.registerSystemProperty("org.apache.ibatis.ognl.security.manager","forceDisableOnInit");
                featureUtils.registerSystemProperty("ognl.security.manager","forceDisableOnInit");
            }, mybatisOgnl);
        }
        Class<?> ognlSecurityManager = featureUtils.loadClass("ognl.security.OgnlSecurityManagerFactory");
        if (ognlSecurityManager != null) {
            access.registerReachabilityHandler(duringAnalysisAccess -> {
                featureUtils.registerSystemProperty("ognl.security.manager","forceDisableOnInit");
            }, ognlSecurityManager);
        }
    }

}
