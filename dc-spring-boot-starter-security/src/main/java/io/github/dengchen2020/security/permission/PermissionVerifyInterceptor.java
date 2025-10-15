package io.github.dengchen2020.security.permission;

import io.github.dengchen2020.core.interceptor.BaseHandlerMethodInterceptor;
import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.security.annotation.HasPermission;
import io.github.dengchen2020.security.exception.NoPermissionException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;

/**
 * 权限校验拦截器
 *
 * @author xiaochen
 * @since 2024/7/22
 */
public class PermissionVerifyInterceptor extends BaseHandlerMethodInterceptor {

    private final PermissionVerifier permissionVerifier;

    public PermissionVerifyInterceptor(PermissionVerifier permissionVerifier) {
        this.permissionVerifier = permissionVerifier;
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request,@Nonnull HttpServletResponse response,@Nonnull HandlerMethod handlerMethod) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        if (authentication instanceof AnonymousAuthentication) return true;
        HasPermission hasPermission = handlerMethod.getMethod().getAnnotation(HasPermission.class);
        if (hasPermission == null) hasPermission = handlerMethod.getBeanType().getAnnotation(HasPermission.class);
        if (hasPermission == null) return true;
        if (permissionVerifier.hasPermission(authentication, hasPermission.value().length == 0 ? new String[]{request.getRequestURI()} : hasPermission.value())) return true;
        throw new NoPermissionException();
    }

}
