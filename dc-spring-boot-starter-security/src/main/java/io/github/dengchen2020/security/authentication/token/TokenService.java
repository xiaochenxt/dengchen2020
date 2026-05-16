package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.security.principal.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;

/**
 * token认证接口
 *
 * @author xiaochen
 * @since 2024/4/24
 */
public interface TokenService {

    /**
     * 从请求中获取token的名称
     */
    String tokenName();

    /**
     * 从请求中获取token
     *
     * @param request 请求上下文对象
     * @return token
     */
    default String getToken(HttpServletRequest request) {
        var tokenName = tokenName();
        String token = request.getHeader(tokenName);
        if (token != null) return token;
        token = request.getParameter(tokenName);
        if (token != null) return token;
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.length() > 7) {
            return authorization.substring(7);
        }
        return token;
    }

    /**
     * 创建token
     *
     * @param authentication 认证信息对象
     * @return Token信息
     */
    TokenInfo createToken(Authentication authentication);

    /**
     * 读取token
     *
     * @param token token值
     * @return 认证信息对象
     */
    Authentication readToken(String token);

}
