package io.github.dengchen2020.ratelimiter;

import io.github.dengchen2020.core.interceptor.BaseHandlerMethodInterceptor;
import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import io.github.dengchen2020.core.utils.RequestUtils;
import io.github.dengchen2020.ratelimiter.annotation.RateLimit;
import io.github.dengchen2020.ratelimiter.annotation.RateLimitStrategy;
import io.github.dengchen2020.ratelimiter.exception.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;

import java.security.Principal;

/**
 * 限流拦截器
 *
 * @author xiaochen
 * @since 2024/8/3
 */
@NullMarked
public abstract class AbstractRateLimiterInterceptor extends BaseHandlerMethodInterceptor {

    private final String errorMsg;

    public AbstractRateLimiterInterceptor(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    protected String getLimitKey(HttpServletRequest request, RateLimit rateLimit, String methodName) {
        RateLimitStrategy strategy = rateLimit.strategy();
        String limitKey;
        switch (strategy) {
            case userAndUri -> {
                Principal principal = request.getUserPrincipal();
                if (principal == null || principal instanceof AnonymousAuthentication) {
                    limitKey = RequestUtils.getRemoteAddr(request) + ":" + methodName;
                } else {
                    limitKey = principal.getName()+ ":" + methodName;
                }
            }
            case ip -> limitKey = RequestUtils.getRemoteAddr(request);
            case ipAndUri -> limitKey = RequestUtils.getRemoteAddr(request) + ":" + methodName;
            case user -> {
                Principal principal = request.getUserPrincipal();
                if (principal == null || principal instanceof AnonymousAuthentication) {
                    limitKey = RequestUtils.getRemoteAddr(request);
                } else {
                    limitKey = principal.getName();
                }
            }
            case uri -> limitKey = methodName;
            case null, default -> limitKey = RequestUtils.getRemoteAddr(request) + ":" + methodName;
        }
        return limitKey;
    }

    protected abstract boolean limit(RateLimit rateLimit, String limitKey);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        RateLimit rateLimit = handlerMethod.getMethod().getAnnotation(RateLimit.class);
        if (rateLimit == null) rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        if (rateLimit == null) return true;
        String limitKey = getLimitKey(request, rateLimit, handlerMethod.toString());
        if (limit(rateLimit, limitKey)) throw new RateLimitException(StringUtils.hasText(rateLimit.errorMsg()) ? rateLimit.errorMsg() : errorMsg, rateLimit.timeUnit());
        return true;
    }
}




