package io.github.dengchen2020.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;

import static io.github.dengchen2020.core.utils.IPUtils.UNKNOWN;

/**
 * 请求扩展工具类
 *
 * @author xiaochen
 * @since 2023/8/22
 */
public abstract class RequestUtils extends RequestContextHolder {

    private static final Logger log = LoggerFactory.getLogger(RequestUtils.class);

    private static final String REAL_CLIENT_IP_HEADER = System.getProperty("dc.real.client.ip.header", "X-Real-IP");

    /**
     * 获取当前请求上下文属性
     */
    @NonNull
    public static ServletRequestAttributes getRequiredRequestAttributes() {
        RequestAttributes requestAttributes = getRequestAttributes();
        if (requestAttributes == null) {
            throw new IllegalStateException("获取当前线程绑定的请求上下文属性失败");
        }
        return (ServletRequestAttributes) requestAttributes;
    }

    /**
     * 获取当前请求
     */
    @NonNull
    public static HttpServletRequest getCurrentRequest() {
        return getRequiredRequestAttributes().getRequest();
    }

    /**
     * 获取web的异步请求管理器，详见：{@link WebAsyncManager}
     */
    @NonNull
    public static WebAsyncManager getAsyncManager() {
        return WebAsyncUtils.getAsyncManager(getCurrentRequest());
    }

    /**
     * 获取接口地址，包含域名、端口
     */
    @NonNull
    public static String getPath() {
        return getPath(getCurrentRequest());
    }

    /**
     * 获取接口地址，包含域名、端口
     *
     * @param request
     */
    @NonNull
    public static String getPath(@NonNull HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        String path = scheme + "://" + request.getServerName();
        if ((scheme.equals("http") && (port != 80))
                || (scheme.equals("https") && (port != 443))) {
            path += ":" + request.getServerPort();
        }
        path += request.getRequestURI();
        return path;
    }

    /**
     * 获取远程客户端ip地址
     * <p>
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     * </p>
     */
    public static String getRemoteAddr(@NonNull HttpServletRequest request) {
        String ip = null;
        try {
            //获取nginx等代理服务器配置的自定义ip请求头的ip
            ip = request.getHeader(REAL_CLIENT_IP_HEADER);
            if (StringUtils.hasText(ip) && !UNKNOWN.equals(ip)) return ip;
            ip = request.getRemoteAddr();
        } catch (Exception e) {
            log.error("获取客户端IP失败: {}", e.toString());
        }
        return ip;
    }

    /**
     * 获取远程客户端ip地址
     * <p>
     * 使用Nginx等反向代理软件， 则不能通过request.getRemoteAddr()获取IP地址
     * 如果使用了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP地址，X-Forwarded-For中第一个非unknown的有效IP字符串，则为真实IP地址
     * </p>
     */
    public static String getRemoteAddr() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) return "";
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return getRemoteAddr(request);
    }

}
