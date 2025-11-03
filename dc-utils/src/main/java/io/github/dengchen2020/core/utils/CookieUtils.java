package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NullMarked;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

/**
 * cookie工具类
 * @author xiaochen
 * @since 2025/4/21
 */
@NullMarked
public abstract class CookieUtils {

    /**
     * 生成cookie header的cookie值
     *
     * @param httpCookie HttpCookie
     * @return cookie值
     */
    public static String parseToString(Collection<HttpCookie> httpCookie) {
        StringJoiner joiner = new StringJoiner(";");
        for (HttpCookie cookie : httpCookie) {
            joiner.add(cookie.toString());
        }
        return joiner.toString();
    }

    /**
     * 生成cookie header的cookie值
     *
     * @param setCookieValue setCookieValue
     * @return cookie值
     */
    public static List<HttpCookie> parse(String setCookieValue) {
        return HttpCookie.parse(setCookieValue);
    }

    /**
     * 生成cookie header的cookie值
     *
     * @param setCookieValue setCookieValue
     * @return cookie值
     */
    public static List<HttpCookie> parse(Collection<String> setCookieValue) {
        List<HttpCookie> httpCookies = new ArrayList<>(setCookieValue.size());
        setCookieValue.forEach(s -> {
            List<HttpCookie> cookies = HttpCookie.parse(s);
            if (!cookies.isEmpty()) httpCookies.add(cookies.getFirst());
        });
        return httpCookies;
    }

}
