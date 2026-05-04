package io.github.dengchen2020.core.redis.frequency;

import io.github.dengchen2020.core.interceptor.BaseHandlerMethodInterceptor;
import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import org.jspecify.annotations.NullMarked;
import org.springframework.web.method.HandlerMethod;

/**
 * 频控拦截器，仅适用于简单场景，仅作用于已登录用户
 *
 * @author xiaochen
 * @since 2025/5/15
 */
@NullMarked
public class SimpleFrequencyControlInterceptor extends BaseHandlerMethodInterceptor {

    private final FrequencyControlSupport frequencyControlSupport;

    public SimpleFrequencyControlInterceptor(FrequencyControlSupport frequencyControlSupport) {
        this.frequencyControlSupport = frequencyControlSupport;
    }

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        FrequencyControl control = handlerMethod.getMethod().getAnnotation(FrequencyControl.class);
        if (control == null)
            control = handlerMethod.getBeanType().getAnnotation(FrequencyControl.class);
        if (control == null) return true;
        if (shouldNotTrigger(request)) return true;
        long res = frequencyControlSupport.trigger(getTriggerKey(request, handlerMethod), control.qps(), control.qpm(), control.qpd());
        if (res == 0) return true;
        handleResult(control, res);
        return true;
    }

    protected boolean shouldNotTrigger(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        return principal == null || principal instanceof AnonymousAuthentication;
    }

    protected String getTriggerKey(HttpServletRequest request, HandlerMethod handlerMethod) {
        Principal principal = request.getUserPrincipal();
        return request.getRequestURI() + ":" + request.getMethod() + ":" + principal.getName();
    }

    protected void handleResult(FrequencyControl control, long res) {
        if (res == 1) {
            throw new FrequencyControlException("触发秒级频次限制，请稍后再试", "qps", control.qps());
        } else if (res == 2) {
            throw new FrequencyControlException("触发分钟级频次限制，请一分钟后再试", "qpm", control.qpm());
        } else if (res == 3) {
            throw new FrequencyControlException("触发天级频次限制，请明天再试", "qpd", control.qpd());
        }
    }

}
