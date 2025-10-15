package io.github.dengchen2020.core.redis.frequency;

import io.github.dengchen2020.core.interceptor.BaseHandlerMethodInterceptor;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.security.Principal;

/**
 * 频控拦截器
 *
 * @author xiaochen
 * @since 2025/5/15
 */
public class FrequencyControlInterceptor extends BaseHandlerMethodInterceptor {

    private final FrequencyControlSupport frequencyControlSupport;

    public FrequencyControlInterceptor(FrequencyControlSupport frequencyControlSupport) {
        this.frequencyControlSupport = frequencyControlSupport;
    }

    @Override
    protected boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull HandlerMethod handlerMethod) {
        FrequencyControl control = handlerMethod.getMethod().getAnnotation(FrequencyControl.class);
        if (control == null)
            control = handlerMethod.getBeanType().getAnnotation(FrequencyControl.class);
        if (control == null) return true;
        Principal principal = request.getUserPrincipal();
        if (principal == null) return true;
        long res = frequencyControlSupport.trigger(request.getRequestURI() + ":" + request.getMethod() + ":" + principal.getName(), control.qps(), control.qpm(), control.qpd());
        if (res == 0) return true;
        if (res == 1) {
            throw new FrequencyControlException("触发秒级频次限制，请稍后再试", "qps", control.qps());
        } else if (res == 2) {
            throw new FrequencyControlException("触发分钟级频次限制，请一分钟后再试", "qpm", control.qpm());
        } else if (res == 3) {
            throw new FrequencyControlException("触发天级频次限制，请明天再试", "qpd", control.qpd());
        }
        return true;
    }

}
