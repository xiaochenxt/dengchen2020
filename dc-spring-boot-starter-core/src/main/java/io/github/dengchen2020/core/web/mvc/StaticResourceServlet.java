package io.github.dengchen2020.core.web.mvc;

import io.github.dengchen2020.core.utils.DateTimeUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.ZoneId;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_BYTE_ARRAY;

/**
 * 为静态资源提供访问缓存（为避免内存占用过多，超过一定大小的资源不会缓存）
 *
 * @author xiaochen
 * @since 2025/8/1
 */
@WebServlet(value = "/**", loadOnStartup = 1)
public class StaticResourceServlet extends HttpServlet implements ApplicationListener<ContextRefreshedEvent> {

    private final ResourceUrlProvider resourceUrlProvider;

    public StaticResourceServlet(Environment environment, ResourceUrlProvider resourceUrlProvider) {
        this.staticPathPattern = environment.getProperty("spring.mvc.static-path-pattern", "/**");
        this.forwardStaticPathPattern = staticPathPattern.replace("/**", "/");
        this.resourceUrlProvider = resourceUrlProvider;
        this.supportHistory = environment.getProperty("dc.static.servlet.support-history", boolean.class, false);
    }

    private static final MethodHandle getResourceMethod;
    private ResourceHttpRequestHandler handler;
    private final String staticPathPattern;
    private final String forwardStaticPathPattern;
    private final boolean supportHistory;

    private final Map<String, CacheEntry> cache = new ConcurrentReferenceHashMap<>();
    private static final int CACHE_EXPIRE_SECONDS = 5;
    private static final int MAX_CACHE_ENTRIES = 2048;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("static-resource-cache-cleaner").factory());
    public static final String indexPath = "/index.html";

    public record ResourceInfo(int status, String contentType, byte[] data, long lastModified, String cacheControl) {
    }

    private record CacheEntry(ResourceInfo resourceInfo, long expireTime) {
        boolean isExpired(long now) {
            return now > expireTime;
        }
    }

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(ResourceHttpRequestHandler.class, MethodHandles.lookup());
            getResourceMethod = lookup.findVirtual(
                    ResourceHttpRequestHandler.class,
                    "getResource",
                    MethodType.methodType(Resource.class, HttpServletRequest.class)
            );
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("未找到方法：org.springframework.web.servlet.resource.ResourceHttpRequestHandler.getResource", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问方法：org.springframework.web.servlet.resource.ResourceHttpRequestHandler.getResource", e);
        }
    }

    @Override
    public void init() {
        scheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, CACHE_EXPIRE_SECONDS, CACHE_EXPIRE_SECONDS + 1, TimeUnit.SECONDS);
    }

    /**
     * <p>{@code handlerMap}是在{@code ContextRefreshedEvent}事件中初始化</p>
     * 详见：{@link ResourceUrlProvider#detectResourceHandlers(ApplicationContext)}
     *
     * @param event
     */
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        handler = resourceUrlProvider.getHandlerMap().get(staticPathPattern);
    }

    @Override
    public void destroy() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private void cleanupExpiredEntries() {
        if (cache.isEmpty()) return;
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        if (uri.isBlank() || uri.equals("/")) uri = indexPath;
        if (useCacheIfPresent(req, resp, uri) != null) return;
        req.setAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, handler);
        handleRequest(req, resp, uri);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp, String uri) throws ServletException, IOException {
        req.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, resourceUrlProvider.getPathMatcher().extractPathWithinPattern(staticPathPattern, forwardStaticPathPattern + uri));
        try {
            handler.handleRequest(req, resp);
        } catch (NoResourceFoundException e) {
            if (supportHistory && !uri.equals(indexPath)) {
                var entry = useCacheIfPresent(req, resp, indexPath);
                cache.put(uri, entry);
                if (entry != null) return;
                handleRequest(req, resp, indexPath);
            } else {
                handleNotFound(resp, uri);
                return;
            }
        }
        Resource resource = null;
        try {
            resource = (Resource) getResourceMethod.invokeExact(handler, req);
            processAndCacheResource(uri, req, resp, resource);
        } catch (Throwable e) {
            throw new ServletException(uri + "资源缓存失败", e);
        } finally {
            if (resource != null) resource.getInputStream().close();
        }
    }

    /**
     * 如果存在缓存就使用它并返回，否则返回null
     */
    private CacheEntry useCacheIfPresent(HttpServletRequest req, HttpServletResponse resp, String uri) throws IOException {
        CacheEntry entry = cache.get(uri);
        if (entry != null) {
            ResourceInfo info = entry.resourceInfo;
            if (req.getHeader(HttpHeaders.RANGE) == null || info.status == HttpServletResponse.SC_NOT_FOUND) {
                handleCachedResource(info, req, resp);
                return entry;
            }
        }
        return entry;
    }

    private void handleNotFound(HttpServletResponse resp, String uri) {
        ResourceInfo info = new ResourceInfo(
                HttpServletResponse.SC_NOT_FOUND,
                resp.getContentType(),
                EMPTY_BYTE_ARRAY,
                -1,
                null
        );
        long expireTime = System.currentTimeMillis() + (CACHE_EXPIRE_SECONDS * 1000);
        cache.put(uri, new CacheEntry(info, expireTime));
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }

    private void handleCachedResource(ResourceInfo info, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType(info.contentType);
        if (info.status == HttpServletResponse.SC_NOT_FOUND) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long lastModified = parseDateHeader(req);
        if (lastModified != -1 && lastModified >= info.lastModified) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getOutputStream().write(info.data);
        }
        if (info.cacheControl != null) resp.setHeader(HttpHeaders.CACHE_CONTROL, info.cacheControl);
    }

    private void processAndCacheResource(String uri, HttpServletRequest req, HttpServletResponse resp, Resource resource) throws IOException {
        try {
            if (req.getHeader(HttpHeaders.RANGE) != null) return;
            long contentLength = resource.contentLength();
            if (contentLength < 0) return;
            if (contentLength <= maxCacheContentLength() && cache.size() < MAX_CACHE_ENTRIES) {
                createAndCacheResourceInfo(uri, resp, resource);
            }
        } catch (NumberFormatException _) {

        }
    }

    private void createAndCacheResourceInfo(String uri, HttpServletResponse resp, Resource resource) throws IOException {
        int status = resp.getStatus();
        if (status == HttpServletResponse.SC_OK || status == HttpServletResponse.SC_NOT_MODIFIED) {
            long lastModified = parseDateValue(resp.getHeader(HttpHeaders.LAST_MODIFIED));
            ResourceInfo info = new ResourceInfo(
                    status,
                    resp.getContentType(),
                    resource.getContentAsByteArray(),
                    lastModified,
                    resp.getHeader(HttpHeaders.CACHE_CONTROL)
            );
            long expireTime = System.currentTimeMillis() + (CACHE_EXPIRE_SECONDS * 1000);
            cache.put(uri, new CacheEntry(info, expireTime));
        } else if (status == HttpServletResponse.SC_NOT_FOUND) {
            ResourceInfo info = new ResourceInfo(
                    HttpServletResponse.SC_NOT_FOUND,
                    resp.getContentType(),
                    EMPTY_BYTE_ARRAY,
                    -1,
                    null
            );
            long expireTime = System.currentTimeMillis() + (CACHE_EXPIRE_SECONDS * 1000);
            cache.put(uri, new CacheEntry(info, expireTime));
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
    }

    protected int maxCacheContentLength() {
        return 1024 * 1024;
    }

    private static final String[] DATE_FORMATS = new String[]{
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };

    private static final ZoneId GMT = TimeZone.getTimeZone("GMT").toZoneId();

    private long parseDateHeader(HttpServletRequest request) {
        long dateValue = -1;
        try {
            dateValue = request.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
        } catch (IllegalArgumentException ex) {
            String headerValue = request.getHeader(HttpHeaders.IF_MODIFIED_SINCE);
            if (headerValue != null) {
                int separatorIndex = headerValue.indexOf(';');
                if (separatorIndex != -1) {
                    String datePart = headerValue.substring(0, separatorIndex);
                    dateValue = parseDateValue(datePart);
                }
            }
        }
        return dateValue;
    }

    protected long parseDateValue(@Nullable String headerValue) {
        if (headerValue == null) return -1;
        if (headerValue.length() >= 3) {
            try {
                return DateTimeUtils.timestamp(DateTimeUtils.parseZoneDateTime(headerValue, GMT, DATE_FORMATS));
            } catch (Exception _) {
            }
        }
        return -1;
    }
}
