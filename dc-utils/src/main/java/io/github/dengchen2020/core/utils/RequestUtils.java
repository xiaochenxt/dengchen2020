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

    private static final String DEFAULT_REAL_IP_HEADER = System.getProperty("dc.real.ip.header","");
    public static final String NGINX_REAL_IP_HEADER = "x-real-ip";
    public static final String CLOUDFLARE_REAL_IP_HEADER = "cf-connecting-ip";
    public static final boolean USE_NGINX = Boolean.parseBoolean(System.getProperty("dc.use-nginx","true"));
    public static final boolean USE_CLOUDFLARE = Boolean.parseBoolean(System.getProperty("dc.use-cloudflare","true"));

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
     * 使用cloudflare的cdn、或Nginx的代理时，不能直接通过request.getRemoteAddr()获取IP地址，获得的ip是代理的ip，
     * 需要从指定请求头中获取，这里默认尝试从cloudflare的ip请求头、nginx的ip请求头、指定请求头中获取用户的真实ip
     * </p>
     */
    public static String getRemoteAddr(@NonNull HttpServletRequest request) {
        String ip = null;
        try {
            if (USE_CLOUDFLARE) {
                //从cloudflare的ip请求头中获取真实用户的ip
                ip = request.getHeader(CLOUDFLARE_REAL_IP_HEADER);
                if (StringUtils.hasText(ip) && !UNKNOWN.equals(ip)) return ip;
            }
            if (USE_NGINX) {
                //从nginx的ip请求头中获取真实用户的ip
                ip = request.getHeader(NGINX_REAL_IP_HEADER);
                if (StringUtils.hasText(ip) && !UNKNOWN.equals(ip)) return ip;
            }
            if (!DEFAULT_REAL_IP_HEADER.isBlank()) {
                ip = request.getHeader(DEFAULT_REAL_IP_HEADER);
                if (StringUtils.hasText(ip) && !UNKNOWN.equals(ip)) return ip;
            }
            ip = request.getRemoteAddr();
        } catch (Exception e) {
            log.error("获取客户端IP失败: {}", e.toString());
        }
        return ip;
    }

    /**
     * 获取远程客户端ip地址
     * <p>
     * 使用cloudflare的cdn、或Nginx的代理时，不能直接通过request.getRemoteAddr()获取IP地址，获得的ip是代理的ip，
     * 需要从指定请求头中获取，这里默认尝试从cloudflare的ip请求头、nginx的ip请求头、指定请求头中获取用户的真实ip
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
