package io.github.dengchen2020.core.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.AsyncWebRequest;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 响应工具类
 *
 * @author xiaochen
 * @since 2025/9/22
 */
public abstract class ResponseUtils {

    /**
     * 获取当前响应
     */
    public static HttpServletResponse getCurrentResponse() {
        return RequestUtils.getRequiredRequestAttributes().getResponse();
    }

    /**
     * 设置请求缓存响应
     *
     * @param time 时间
     */
    public static void cache(Duration time) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletResponse response = servletRequestAttributes.getResponse();
            if (response != null) {
                response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=" + time.toSeconds() + ", must-revalidate, private, proxy-revalidate");
            }
        }
    }

    /**
     * 请求缓存响应
     *
     * @param body 内容
     * @param time 时间
     * @param <T>  返回值
     * @return 响应数据
     */
    public static <T> T cache(T body, Duration time) {
        cache(time);
        return body;
    }

    /**
     * 请求缓存响应，执行时间超过指定时间才缓存
     *
     * @param time 时间
     * @param func 执行方法
     * @param executeTimeMs 执行时间（单位毫秒）
     * @param <T>  返回值
     * @return 响应数据
     */
    public static <T> T cache(Duration time, Supplier<T> func, long executeTimeMs) {
        long startTime = System.currentTimeMillis();
        T res = func.get();
        long endTime = System.currentTimeMillis();
        if (endTime - startTime > executeTimeMs) {
            cache(time);
        }
        return res;
    }

    /**
     * 请求缓存响应，执行时间超过指定时间缓存{@code time}时间，否则缓存{@code elseTime}时间
     *
     * @param time 时间
     * @param func 执行方法
     * @param executeTimeMs 执行时间（单位毫秒）
     * @param <T>  返回值
     * @return 响应数据
     */
    public static <T> T cache(Duration time, Duration elseTime, Supplier<T> func, long executeTimeMs) {
        long startTime = System.currentTimeMillis();
        T res = func.get();
        long endTime = System.currentTimeMillis();
        if (endTime - startTime > executeTimeMs) {
            cache(time);
        }else {
            cache(elseTime);
        }
        return res;
    }

    /**
     * 写入客户端Cookie
     * @param cookie {@link ResponseCookie}
     */
    public static void addCookie(ResponseCookie cookie) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletResponse response = servletRequestAttributes.getResponse();
            if (response != null) {
                response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            }
        }
    }

    /**
     * 写入允许跨域客户端Cookie
     * <p>浏览器默认不携带跨域Cookie，需额外配置，如：axios.defaults.withCredentials = true;</p>
     * @param name 键
     * @param value 值
     * @param maxAge 有效期
     * @param domain 有效域名
     * @param path 有效路径
     */
    public static void addCorsSiteCookie(String name, String value, Duration maxAge, boolean secure, String domain, String path) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletResponse response = servletRequestAttributes.getResponse();
            if (response != null) {
                ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie.from(name, value)
                        // .sameSite(Cookie.SameSite.NONE.attributeValue())
                        .secure(secure)
                        .httpOnly(true);
                if (maxAge != null) cookie.maxAge(maxAge);
                if (domain != null) cookie.domain(domain);
                if (path != null) cookie.path(path);
                response.addHeader(HttpHeaders.SET_COOKIE, cookie
                        .build()
                        .toString());
            }
        }
    }

    /**
     * 写入允许跨域客户端Cookie
     * <p>浏览器默认不携带跨域Cookie，需额外配置，如：axios.defaults.withCredentials = true;</p>
     * @param name 键
     * @param value 值
     * @param maxAge 有效期
     */
    public static void addCorsSiteCookie(String name, String value, Duration maxAge, boolean secure) {
        addCorsSiteCookie(name, value, maxAge, secure, null, "/");
    }

    /**
     * 写入允许跨域客户端Cookie
     * <p>浏览器默认不携带跨域Cookie，需额外配置，如：axios.defaults.withCredentials = true;</p>
     * @param name 键
     * @param value 值
     * @param maxAge 有效期
     */
    public static void addCorsSiteCookie(String name, String value, Duration maxAge) {
        addCorsSiteCookie(name, value, maxAge, true, null, "/");
    }

    /**
     * 写入允许跨域客户端永久Cookie
     * <p>浏览器默认不携带跨域Cookie，需额外配置，如：axios.defaults.withCredentials = true;</p>
     * @param name 键
     * @param value 值
     */
    public static void addCorsSiteCookie(String name, String value) {
        addCorsSiteCookie(name, value, null, true, null, "/");
    }

    /**
     * 基于 {@link StreamingResponseBody} 的流式写入，增加超时时间配置
     * <p>示例：</p>
     * <pre>
     * {@code
     * return ResponseUtils.stream(out -> {
     *     for(int i=0;i<100;i++){
     *         var dto = new DTO();
     *         dto.setA("114.114.114.114");
     *         dto.setB(123456789);
     *         dto.setC(i);
     *         out.write(JsonUtils.serialize(dto));
     *         out.write("\n".getBytes());
     *         out.flush();
     *     }
     * }, Duration.ofSeconds(600));
     * }
     * </pre>
     * <p>前端使用fetch-bak.js中的示例代码完成流式处理</p>
     * @param streamingResponseBody {@link StreamingResponseBody}
     * @param timeout 超时时间
     * @return
     */
    public static StreamingResponseBody stream(StreamingResponseBody streamingResponseBody, Duration timeout) {
        AsyncWebRequest request = RequestUtils.getAsyncManager().getAsyncWebRequest();
        request.setTimeout(timeout.toMillis());
        return streamingResponseBody;
    }

}
