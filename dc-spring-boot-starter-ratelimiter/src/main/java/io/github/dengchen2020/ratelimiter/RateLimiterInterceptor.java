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
import java.util.concurrent.TimeUnit;

/**
 * 限流拦截器
 *
 * @author xiaochen
 * @since 2024/8/3
 */
@NullMarked
public class RateLimiterInterceptor extends BaseHandlerMethodInterceptor {

    private final RateLimiter secondRateLimiter;

    private final RateLimiter minuteRateLimiter;

    private final String errorMsg;

    public RateLimiterInterceptor(RateLimiter secondRateLimiter, RateLimiter minuteRateLimiter, String errorMsg) {
        this.secondRateLimiter = secondRateLimiter;
        this.minuteRateLimiter = minuteRateLimiter;
        this.errorMsg = errorMsg;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {
        RateLimit rateLimit = handlerMethod.getMethod().getAnnotation(RateLimit.class);
        if (rateLimit == null) rateLimit = handlerMethod.getBeanType().getAnnotation(RateLimit.class);
        if (rateLimit == null) return true;
        RateLimitStrategy strategy = rateLimit.strategy();
        String limitKey;
        switch (strategy) {
            case userAndUri -> {
                Principal principal = request.getUserPrincipal();
                if (principal == null || principal instanceof AnonymousAuthentication) {
                    limitKey = RequestUtils.getRemoteAddr(request) + request.getRequestURI() + request.getMethod();
                } else {
                    limitKey = principal.getName() + request.getRequestURI() + request.getMethod();
                }
            }
            case ip -> limitKey = RequestUtils.getRemoteAddr(request);
            case ipAndUri -> limitKey = RequestUtils.getRemoteAddr(request) + request.getRequestURI() + request.getMethod();
            case user -> {
                Principal principal = request.getUserPrincipal();
                if (principal == null || principal instanceof AnonymousAuthentication) {
                    limitKey = RequestUtils.getRemoteAddr(request);
                } else {
                    limitKey = principal.getName();
                }
            }
            case uri -> limitKey = request.getRequestURI() + request.getMethod();
            case null, default -> limitKey = RequestUtils.getRemoteAddr(request) + request.getRequestURI() + request.getMethod();
        }
        RateLimiter rateLimiter;
        if (rateLimit.timeUnit() == TimeUnit.MINUTES) {
            rateLimiter = minuteRateLimiter;
        } else {
            rateLimiter = secondRateLimiter;
        }
        if (rateLimiter.limit(limitKey, rateLimit.value())) throw new RateLimitException(StringUtils.hasText(rateLimit.errorMsg()) ? rateLimit.errorMsg() : errorMsg);
        return true;
    }
}




