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

    private static final Playwright PLAYWRIGHT;
    private static final Browser BROWSER;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.95 Safari/537.36";

    static {
        PLAYWRIGHT = create();
        BROWSER = PLAYWRIGHT.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                BROWSER.close();
            } finally {
                PLAYWRIGHT.close();
            }
        }));
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
    private static Playwright create(Playwright.@Nullable CreateOptions options) {
        install();
        return PlaywrightImpl.create(options);
    }

    private static Playwright create() {
        return create(null);
    }

    /**
     * 如果未安装浏览器，则安装浏览器
     */
    private static void install() {
        try {
            Driver driver = Driver.ensureDriverInstalled(Collections.emptyMap(), false);
            ProcessBuilder pb = driver.createProcessBuilder();
            pb.command().add("install");
            String version = Playwright.class.getPackage().getImplementationVersion();
            if (version != null) pb.environment().put("PW_CLI_DISPLAY_VERSION", version);
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } catch (IOException e) {
            throw new RuntimeException("安装浏览器失败", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static Browser newChromiumBrowser(BrowserType.LaunchOptions launchOptions) {
        return PLAYWRIGHT.chromium().launch(launchOptions);
    }

    public static Browser newFirefoxBrowser(BrowserType.LaunchOptions launchOptions) {
        return PLAYWRIGHT.firefox().launch(launchOptions);
    }

    public static Browser newWebkitBrowser(BrowserType.LaunchOptions launchOptions) {
        return PLAYWRIGHT.webkit().launch(launchOptions);
    }

    /**
     * 使用现有的浏览器实例创建隔离的上下文环境执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param browser 浏览器
     * @param contextOptions 上下文选项
     * @param pageConsumers 页面操作
     */
    @SafeVarargs
    public static void execute(Browser browser, Browser.NewContextOptions contextOptions, Consumer<Page>... pageConsumers) {
        CountDownLatch countDownLatch = new CountDownLatch(pageConsumers.length);
        for (Consumer<Page> consumer : pageConsumers) {
            Thread.startVirtualThread(() -> {
                try (var context = browser.newContext(contextOptions)) {
                    consumer.accept(context.newPage());
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
     * @param browser 浏览器
     * @param pageConsumers 页面操作
     */
    @SafeVarargs
    public static void execute(Browser browser, Consumer<Page>... pageConsumers) {
        execute(browser, new Browser.NewContextOptions().setUserAgent(USER_AGENT), pageConsumers);
    }

    /**
     * 使用现有的浏览器实例创建隔离的上下文环境执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param pageConsumer 页面操作
     */
    @SafeVarargs
    public static void execute(Consumer<Page>... pageConsumer) {
        execute(BROWSER, pageConsumer);
    }

    /**
     * 使用现有的浏览器实例创建隔离的上下文环境执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param browser 浏览器
     * @param contextOptions 上下文选项
     * @param pageFunc 页面操作
     */
    public static <R> R executeWithResult(Browser browser, Browser.NewContextOptions contextOptions, Function<Page, R> pageFunc) {
        try (BrowserContext context = browser.newContext(contextOptions)) {
            return pageFunc.apply(context.newPage());
        }
    }

    /**
     * 使用现有的浏览器实例创建隔离的上下文环境执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param browser 浏览器
     * @param pageFunc 页面操作
     */
    public static <R> R executeWithResult(Browser browser, Function<Page, R> pageFunc) {
        return executeWithResult(browser, new Browser.NewContextOptions().setUserAgent(USER_AGENT), pageFunc);
    }

    /**
     * 使用现有的浏览器实例创建隔离的上下文环境执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param pageFunc 页面操作
     */
    public static <R> R executeWithResult(Function<Page, R> pageFunc) {
        return executeWithResult(BROWSER, pageFunc);
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
