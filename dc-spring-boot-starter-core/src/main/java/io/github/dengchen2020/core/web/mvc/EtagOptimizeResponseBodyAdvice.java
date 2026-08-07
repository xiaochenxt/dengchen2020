package io.github.dengchen2020.core.web.mvc;

import io.github.dengchen2020.core.utils.RequestUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 避免etag导致的一些副作用
 *
 * @author xiaochen
 * @since 2025/4/16
 */
@NullMarked
@ControllerAdvice
public class EtagOptimizeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType)
                || StringHttpMessageConverter.class.isAssignableFrom(converterType)
                || ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)
        ) {
            return false;
        }
        ShallowEtagHeaderFilter.disableContentCaching(RequestUtils.getCurrentRequest());
        return false;
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return body;
    }

}
