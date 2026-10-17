package io.github.dengchen2020.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.SpringProperties;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;

/**
 * 请求扩展工具类
 *
 * @author xiaochen
 * @since 2023/8/22
 */
public abstract class RequestUtils {

    private static final Logger log = LoggerFactory.getLogger(RequestUtils.class);

    /**
     * 从CDN获取客户端真实IP地址的请求头
     */
    public static final String CDN_IP_HEADER = SpringProperties.getProperty("dc.cdn.ip.header");
    /**
     * 启用Cloudflare代理时Cloudflare会将客户端真实IP地址放在这个请求头中
     */
    public static final String CLOUDFLARE_IP_HEADER = "cf-connecting-ip";

    /**
     * 获取当前请求上下文属性
     */
    public static @NonNull ServletRequestAttributes getRequiredRequestAttributes() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) throw new IllegalStateException("获取当前线程绑定的请求上下文属性失败");
        return (ServletRequestAttributes) requestAttributes;
    }

    /**
     * 获取当前请求
     */
    public static @NonNull HttpServletRequest getCurrentRequest() {
        return getRequiredRequestAttributes().getRequest();
    }

    /**
     * 获取web的异步请求管理器，详见：{@link WebAsyncManager}
     */
    public static @NonNull WebAsyncManager getAsyncManager() {
        return WebAsyncUtils.getAsyncManager(getCurrentRequest());
    }

    /**
     * 获取接口地址，包含域名、端口
     */
    public static @NonNull String getRequestURL() {
        return getCurrentRequest().getRequestURL().toString();
    }

    /**
     * 获取远程客户端ip地址
     */
    public static String getRemoteAddr(@NonNull HttpServletRequest request) {
        String ip = null;
        try {
            if (CDN_IP_HEADER != null) {
                //从cdn的ip请求头中获取客户端真实IP地址
                ip = request.getHeader(CDN_IP_HEADER);
                if (ip != null) return ip;
            }
            ip = request.getRemoteAddr();
        } catch (Exception e) {
            log.error("获取客户端IP失败: {}", e.toString());
        }
        return ip;
    }

    /**
     * 获取远程客户端ip地址
     */
    public static String getRemoteAddr() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return "";
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return getRemoteAddr(request);
    }

}
