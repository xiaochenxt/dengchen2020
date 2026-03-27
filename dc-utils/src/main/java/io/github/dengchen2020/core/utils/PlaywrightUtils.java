package io.github.dengchen2020.core.utils;

import com.microsoft.playwright.*;
import com.microsoft.playwright.impl.PlaywrightImpl;
import com.microsoft.playwright.impl.driver.Driver;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 浏览器自动化测试工具类
 * @author xiaochen
 * @since 2025/12/11
 */
@NullMarked
public abstract class PlaywrightUtils {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.95 Safari/537.36";
    /**
     * 是否启用无头模式，默认启用。建议只在开发阶段关闭
     */
    public static final String HEADLESS = "playwright.headless";
    /**
     * 是否使用新版无头模式，默认使用新版。建议只在对性能有要求并且开发阶段已确认旧版无头模式运行没有问题时才使用旧版
     * <p>1.旧版无头模式是 Chromium 的 //content 模块的轻量级封装容器，因此依赖项要少得多。具体而言，它不需要 X11/Wayland、D-Bus，并且在某些方面比完整的 Chrome 浏览器的性能更出色。因此，它适用于自动截取屏幕截图或 Web 抓取等用例。</p>
     * <p>1.另一方面，新版无头 Chrome 是真正的 Chrome 浏览器，因此更真实、更可靠，并且提供更多功能。因此，它更适合高精度端到端 Web 应用测试或浏览器扩展程序测试。</p>
     */
    public static final String HEADLESS_NEW = "playwright.headless.new";
    private static final boolean headless = Boolean.parseBoolean(System.getProperty(HEADLESS, "true"));
    private static final boolean headlessNew = Boolean.parseBoolean(System.getProperty(HEADLESS_NEW, "true"));

    static {
        install();
    }

    /**
     * 启动新的Playwright驱动进程并连接到它。 {@link com.microsoft.playwright.Playwright#close
     * Playwright.close()} 当实例不再需要时，应调用。
     * <pre>{@code
     * Playwright playwright = Playwright.create();
     * Browser browser = playwright.webkit().launch();
     * Page page = browser.newPage();
     * page.navigate("https://www.w3.org/");
     * playwright.close();
     * }</pre>
     *
     */
    public static Playwright create(Playwright. @Nullable CreateOptions options) {
        return PlaywrightImpl.create(options);
    }

    /**
     * 启动新的Playwright驱动进程并连接到它。 {@link com.microsoft.playwright.Playwright#close
     * Playwright.close()} 当实例不再需要时，应调用。
     * <pre>{@code
     * Playwright playwright = Playwright.create();
     * Browser browser = playwright.webkit().launch();
     * Page page = browser.newPage();
     * page.navigate("https://www.w3.org/");
     * playwright.close();
     * }</pre>
     *
     */
    public static Playwright create() {
        return create(null);
    }

    /**
     * 如果未安装浏览器，则安装浏览器
     */
    private static void install() {
        try {
            var driver = Driver.ensureDriverInstalled(Collections.emptyMap(), false);
            var pb = driver.createProcessBuilder();
            pb.command().add("install");
            var version = Playwright.class.getPackage().getImplementationVersion();
            if (version != null) pb.environment().put("PW_CLI_DISPLAY_VERSION", version);
            pb.inheritIO();
            var process = pb.start();
            process.waitFor();
        } catch (IOException e) {
            throw new RuntimeException("安装浏览器失败", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static Browser newChromiumBrowser(Playwright playwright) {
        var launchOptions = new BrowserType.LaunchOptions().setHeadless(headless);
        if (headlessNew) launchOptions.setChannel("chromium");
        return playwright.chromium().launch(launchOptions);
    }

    /**
     * 使用现有的浏览器实例创建隔离的上下文环境执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param contextOptions 上下文选项
     * @param pageConsumers 页面操作
     */
    @SafeVarargs
    public static void execute(Browser.NewContextOptions contextOptions, Consumer<Page>... pageConsumers) {
        var countDownLatch = new CountDownLatch(pageConsumers.length);
        for (var consumer : pageConsumers) {
            Thread.startVirtualThread(() -> {
                try {
                    try (var playwright = create();
                         var browser = newChromiumBrowser(playwright);
                         var context = browser.newContext(contextOptions)) {
                        consumer.accept(context.newPage());
                    }
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 使用现有的浏览器实例创建隔离的上下文环境执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param pageConsumers 页面操作
     */
    @SafeVarargs
    public static void execute(Consumer<Page>... pageConsumers) {
        execute(new Browser.NewContextOptions().setUserAgent(USER_AGENT), pageConsumers);
    }

    /**
     * 使用现有的浏览器实例创建隔离的上下文环境执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param contextOptions 上下文选项
     * @param pageFunc 页面操作
     */
    public static <R> R executeWithResult(Browser.NewContextOptions contextOptions, Function<Page, R> pageFunc) {
        try (var playwright = create();
             var browser = newChromiumBrowser(playwright);
             var context = browser.newContext(contextOptions)) {
            return pageFunc.apply(context.newPage());
        }
    }

    /**
     * 使用现有的浏览器实例创建隔离的上下文环境执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param pageFunc 页面操作
     */
    public static <R> R executeWithResult(Function<Page, R> pageFunc) {
        return executeWithResult(new Browser.NewContextOptions().setUserAgent(USER_AGENT), pageFunc);
    }

    /**
     * 生成PDF
     * @param pdfOptions pdf选项
     * @param content 内容，如果是url链接则导航到该链接，否则设置页面内容，最后打印成pdf并返回字节数组
     * @return
     */
    public static byte[] pdf(Page.PdfOptions pdfOptions, String content) {
        return executeWithResult(page -> {
            if (content.startsWith("http") || content.startsWith("file:")) {
                page.navigate(content);
            } else {
                page.setContent(content);
            }
            return page.pdf(pdfOptions);
        });
    }

    /**
     * 生成PDF
     * @param content 内容，如果是url链接则导航到该链接，否则设置页面内容，最后打印成pdf并返回字节数组
     * @return
     */
    public static byte[] pdf(String content) {
        return pdf(new Page.PdfOptions().setFormat("A4"), content);
    }

    /**
     * 截图
     * @param screenshotOptions 截图选项
     * @param content 内容，如果是url链接则导航到该链接，否则设置页面内容，最后截图并返回字节数组
     * @return
     */
    public static byte[] screenshot(Page.ScreenshotOptions screenshotOptions, String content) {
        return executeWithResult(page -> {
            if (content.startsWith("http") || content.startsWith("file:")) {
                page.navigate(content);
            } else {
                page.setContent(content);
            }
            return page.screenshot(screenshotOptions);
        });
    }

    /**
     * 截图
     * @param content 内容，如果是url链接则导航到该链接，否则设置页面内容，最后截图并返回字节数组
     * @return
     */
    public static byte[] screenshot(String content) {
        return screenshot(new Page.ScreenshotOptions().setFullPage(true), content);
    }

}
