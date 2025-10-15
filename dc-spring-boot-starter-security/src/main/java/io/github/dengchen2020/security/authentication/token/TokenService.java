package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.security.principal.Authentication;
import jakarta.servlet.http.HttpServletRequest;

/**
 * token认证接口
 *
 * @author xiaochen
 * @since 2024/4/24
 */
public interface TokenService {

    String TOKEN_COMMON_PREFIX = "dc:security:token:";

    String TOKEN_INFO_KEY = "dc:security:token:info:";

    /**
     * 从请求中获取token
     *
     * @param request 请求上下文对象
     * @return token
     */
    default String getToken(HttpServletRequest request) {
        String token = request.getHeader(TokenConstant.TOKEN_NAME);
        if (token != null) return token;
        return request.getParameter(TokenConstant.TOKEN_NAME);
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
