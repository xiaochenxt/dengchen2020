package io.github.dengchen2020.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
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
public abstract class RequestUtils extends RequestContextHolder {

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
    public static HttpServletRequest getCurrentRequest() {
        return getRequiredRequestAttributes().getRequest();
    }

    /**
     * 获取web的异步请求管理器，详见：{@link WebAsyncManager}
     */
    public static WebAsyncManager getAsyncManager() {
        return WebAsyncUtils.getAsyncManager(getCurrentRequest());
    }

    /**
     * 获取接口地址，包含域名、端口
     */
    public static String getPath() {
        return getPath(getCurrentRequest());
    }

    /**
     * 获取接口地址，包含域名、端口
     *
     * @param request
     */
    public static String getPath(HttpServletRequest request) {
        if (request == null) return null;
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

}
