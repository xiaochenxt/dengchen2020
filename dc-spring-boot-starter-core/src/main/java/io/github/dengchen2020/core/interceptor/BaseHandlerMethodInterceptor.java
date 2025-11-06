package io.github.dengchen2020.core.interceptor;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 控制器的拦截器基类
 * @author xiaochen
 * @since 2025/2/25
 */
@NullMarked
public class BaseHandlerMethodInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if(!(handler instanceof HandlerMethod handlerMethod) || DispatcherType.ERROR == request.getDispatcherType() || DispatcherType.ASYNC == request.getDispatcherType()) return true;
        return preHandle(request, response, handlerMethod);
    }

    /**
     * 执行处理程序之前的拦截点。调用
     * HandlerMapping 确定了适当的处理程序对象，但在
     * HandlerAdapter 调用处理程序。
     * DispatcherServlet 处理执行链中的处理程序，包括
     * 任意数量的拦截器，处理程序本身位于末尾。
     * 使用此方法，每个拦截器都可以决定中止执行链，
     * 通常发送 HTTP 错误或编写自定义响应。
     * <p><strong>注意：</strong>异步
     * 请求处理。有关更多详细信息，请参阅
     * {@link org.springframework.web.servlet.AsyncHandlerInterceptor}.
     * <p>默认实现返回 {@code true}.
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler 选择要执行的处理程序，用于类型和/或实例评估
     * @return {@code true} 如果执行链应继续执行
     * next interceptor 或处理程序本身。否则，DispatcherServlet 假定
     * 此拦截器已经处理了响应本身。
     */
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) {
        return true;
    }

}
