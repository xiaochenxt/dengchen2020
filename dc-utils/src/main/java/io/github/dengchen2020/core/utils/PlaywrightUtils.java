package io.github.dengchen2020.core.utils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.impl.PlaywrightImpl;
import com.microsoft.playwright.impl.driver.Driver;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * 浏览器自动化测试工具类
 * @author xiaochen
 * @since 2025/12/11
 */
@NullMarked
public abstract class PlaywrightUtils {

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
    public static Playwright create(Playwright.@Nullable CreateOptions options) {
        install();
        return PlaywrightImpl.create(options);
    }

    public static Playwright create() {
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

    /**
     * 创建一个浏览器实例并执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param launchOptions 启动选项
     * @param pageConsumers 页面操作
     */
    @SafeVarargs
    public static void execute(BrowserType.LaunchOptions launchOptions, Consumer<Page>... pageConsumers) {
        CountDownLatch countDownLatch = new CountDownLatch(pageConsumers.length);
        for (Consumer<Page> consumer : pageConsumers) {
            Thread.startVirtualThread(() -> {
                try {
                    try (var playwright = PlaywrightUtils.create();
                         var browser = playwright.chromium().launch(launchOptions)
                    ) {
                        var context = browser.newContext(new Browser.NewContextOptions()
                                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.6261.95 Safari/537.36")
                                .setLocale("zh-CN"));
                        var page = context.newPage();
                        consumer.accept(page);
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
     * 创建一个浏览器实例并执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param headless 是否无头模式
     * @param pageConsumer 页面操作
     */
    @SafeVarargs
    public static void execute(boolean headless, Consumer<Page>... pageConsumer) {
        execute(new BrowserType.LaunchOptions().setHeadless(headless), pageConsumer);
    }

    /**
     * 创建一个浏览器实例并执行页面操作，确保在完成时关闭浏览器实例。 </br>
     * 主要用于简化配置并作为一个启动示例作为参考。
     * @param pageConsumer 页面操作
     */
    @SafeVarargs
    public static void execute(Consumer<Page>... pageConsumer) {
        execute(false, pageConsumer);
    }

}
