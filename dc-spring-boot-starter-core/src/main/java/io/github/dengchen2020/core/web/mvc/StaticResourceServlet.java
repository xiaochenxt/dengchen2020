package io.github.dengchen2020.core.web.mvc;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeansException;
import org.springframework.boot.web.context.WebServerGracefulShutdownLifecycle;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.io.IOException;

/**
 * 为静态资源提供根路径访问
 *
 * @author xiaochen
 * @since 2025/8/1
 */
@WebServlet(value = "/**", loadOnStartup = 1)
public class StaticResourceServlet extends HttpServlet implements ApplicationContextAware, SmartLifecycle {

    public static final String SERVLET_NAME = "staticResourceServlet";

    private volatile boolean running = false;

    public StaticResourceServlet(Environment environment) {
        this.staticPathPattern = environment.getProperty("spring.mvc.static-path-pattern", "/**");
        this.supportHistory = environment.getProperty("dc.static.servlet.support-history", boolean.class, false);
        if (this.supportHistory) {
            this.notFoundHtmlEnabled = false;
        } else {
            this.notFoundHtmlEnabled = environment.getProperty("dc.static.servlet.404-html.enabled", boolean.class, false);
        }
    }

    private ResourceHttpRequestHandler handler;
    private final String staticPathPattern;
    private final boolean supportHistory;
    private final boolean notFoundHtmlEnabled;

    public static final String indexPath = "index.html";
    public static final String notFoundPath = "404.html";

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.isBlank()) {
            uri = indexPath;
        } else if (uri.charAt(uri.length() - 1) == '/') {
            uri = uri + indexPath;
        }
        req.setAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, handler);
        handleRequest(req, resp, uri);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String uri) throws ServletException, IOException {
        req.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, uri);
        try {
            handler.handleRequest(req, resp);
        } catch (NoResourceFoundException e) {
            if (supportHistory && !uri.equals(indexPath)) {
                handleRequest(req, resp, indexPath);
            } else if (notFoundHtmlEnabled && !uri.equals(notFoundPath)) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                handleRequest(req, resp, notFoundPath);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    /**
     * 在web服务准备就绪时查找需要的静态资源处理器 </br>
     * 静态资源处理器获取代码参考：{@link ResourceUrlProvider#detectResourceHandlers(ApplicationContext)}
     */
    @Override
    public void start() {
        running = true;
        applicationContext.getBeanProvider(HandlerMapping.class).orderedStream()
                .filter(AbstractUrlHandlerMapping.class::isInstance)
                .map(AbstractUrlHandlerMapping.class::cast)
                .forEach(mapping -> mapping.getHandlerMap().forEach((pattern, handler) -> {
                    if (handler instanceof ResourceHttpRequestHandler resourceHandler) {
                        if (pattern.equals(this.staticPathPattern)) this.handler = resourceHandler;
                    }
                }));
        if (this.handler == null) throw new IllegalStateException("未找到处理"+this.staticPathPattern+"的静态资源处理器");
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return WebServerGracefulShutdownLifecycle.SMART_LIFECYCLE_PHASE - 5000;
    }

}
