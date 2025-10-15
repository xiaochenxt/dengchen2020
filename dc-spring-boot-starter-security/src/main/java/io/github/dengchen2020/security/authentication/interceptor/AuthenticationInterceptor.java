package io.github.dengchen2020.security.authentication.interceptor;

import io.github.dengchen2020.core.interceptor.BaseHandlerMethodInterceptor;
import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import io.github.dengchen2020.core.utils.DateTimeUtils;
import io.github.dengchen2020.security.annotation.NoTokenRequired;
import io.github.dengchen2020.security.exception.SessionTimeOutException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;

import java.time.LocalDateTime;

/**
 * 身份认证拦截器
 * @author xiaochen
 * @since 2024/4/24
 */
public class AuthenticationInterceptor extends BaseHandlerMethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull HandlerMethod handlerMethod) {
        if (SecurityContextHolder.getAuthentication() != null) return true;
        NoTokenRequired noTokenRequired = handlerMethod.getMethod().getAnnotation(NoTokenRequired.class);
        if (noTokenRequired == null) noTokenRequired = handlerMethod.getBeanType().getAnnotation(NoTokenRequired.class);
        if (noTokenRequired != null) {
            if (SecurityContextHolder.getAuthentication() == null) SecurityContextHolder.setAuthentication(AnonymousAuthentication.INSTANCE);
            if (noTokenRequired.deadlineString().isBlank()) return true;
            try {
                LocalDateTime deadline = DateTimeUtils.parseDateTime(noTokenRequired.deadlineString());
                if (LocalDateTime.now().isBefore(deadline)) return true;
            } catch (Exception e) {
                log.warn("@NoTokenRequired的deadlineString值解析异常，{}", e.toString());
            }
        }
        throw new SessionTimeOutException();
    }

}
