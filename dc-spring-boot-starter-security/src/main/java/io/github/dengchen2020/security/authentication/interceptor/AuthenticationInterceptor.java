package io.github.dengchen2020.security.authentication.interceptor;

import io.github.dengchen2020.core.interceptor.BaseHandlerMethodInterceptor;
import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import io.github.dengchen2020.security.annotation.NoTokenRequired;
import io.github.dengchen2020.security.exception.SessionTimeOutException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;

/**
 * 身份认证拦截器
 * @author xiaochen
 * @since 2024/4/24
 */
public class AuthenticationInterceptor extends BaseHandlerMethodInterceptor {

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull HandlerMethod handlerMethod) {
        if (SecurityContextHolder.getAuthentication() != null) return true;
        NoTokenRequired noTokenRequired = handlerMethod.getMethod().getAnnotation(NoTokenRequired.class);
        if (noTokenRequired == null) noTokenRequired = handlerMethod.getBeanType().getAnnotation(NoTokenRequired.class);
        if (noTokenRequired != null) {
            if (SecurityContextHolder.getAuthentication() == null) SecurityContextHolder.setAuthentication(AnonymousAuthentication.INSTANCE);
            return true;
        }
        throw new SessionTimeOutException();
    }

}
