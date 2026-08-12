package io.github.dengchen2020.core.filter;

import io.github.dengchen2020.core.utils.Base64Utils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 基础认证抽象类
 *
 * @author xiaochen
 * @since 2025/5/9
 */
public abstract class AbstractBasicAuthorizationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (onlyHttps() && !"https".equals(request.getScheme())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            noAuthorization(request, response);
            return;
        }
        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        String credentials = Base64Utils.decodeToString(base64Credentials);
        String[] values = credentials.split(":", 2);
        if (values.length != 2) {
            noAuthorization(request, response);
            return;
        }
        String username = values[0];
        String password = values[1];
        if (handle(username, password, request, response)) {
            filterChain.doFilter(request, response);
        }else {
            noAuthorization(request, response);
        }
    }

    /**
     * 处理用户名和密码，返回true则继续下一个过滤器
     * @param username 用户名
     * @param password 密码
     * @param request
     * @param response
     * @return
     */
    protected boolean handle(String username, String password, HttpServletRequest request, HttpServletResponse response) {
        return true;
    }

    /**
     * 没有认证信息，返回 401 并设置 WWW-Authenticate 头，触发浏览器弹窗
     *
     * @param request
     * @param response
     */
    private void noAuthorization(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + getRealm(request) + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * 认证区域
     * @return
     */
    protected String getRealm(HttpServletRequest request) {
        return getFilterName();
    }

    /**
     * 仅https请求可用，账号密码只是base64编码，并未加密
     * @return
     */
    protected boolean onlyHttps() {
        return true;
    }

}