package io.github.dengchen2020.security.authentication.filter;

import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.security.authentication.token.TokenService;
import io.github.dengchen2020.security.authentication.web.AuthenticationHttpServletRequestWrapper;
import io.github.dengchen2020.core.security.principal.Authentication;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 身份认证过滤器
 *
 * @author xiaochen
 * @since 2023/10/13
 */
public class AuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final TokenService tokenService;

    public AuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response, @Nonnull final FilterChain filterChain) throws IOException, ServletException {
        try {
            String token = tokenService.getToken(request);
            if (StringUtils.hasText(token)) {
                Authentication authentication = tokenService.readToken(token);
                if (authentication != null) {
                    SecurityContextHolder.setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) log.debug("token读取异常：", e);
        } finally {
            try {
                filterChain.doFilter(new AuthenticationHttpServletRequestWrapper(request), response);
            } finally {
                SecurityContextHolder.clear();
            }
        }
    }

}
