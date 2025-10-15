package io.github.dengchen2020.core.web.mvc;

import io.github.dengchen2020.core.utils.RequestUtils;
import io.github.dengchen2020.core.utils.ResponseUtils;
import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * 避免etag导致的一些副作用
 *
 * @author xiaochen
 * @since 2025/4/16
 */
@ControllerAdvice
public class EtagOptimizeResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final long maxContentCachingLength;

    public EtagOptimizeResponseBodyAdvice(long maxContentCachingLength) {
        super();
        if (maxContentCachingLength < 2048) throw new IllegalArgumentException("maxContentCachingLength 必须大于等于2048");
        this.maxContentCachingLength = maxContentCachingLength;
    }

    @Override
    public boolean supports(@Nonnull MethodParameter returnType, @Nonnull Class<? extends HttpMessageConverter<?>> converterType) {
        if (!AbstractJackson2HttpMessageConverter.class.isAssignableFrom(converterType) && !StringHttpMessageConverter.class.isAssignableFrom(converterType) && !AbstractJsonHttpMessageConverter.class.isAssignableFrom(converterType)
                && !ResourceHttpMessageConverter.class.isAssignableFrom(converterType) && !ByteArrayHttpMessageConverter.class.isAssignableFrom(converterType)) {
            ShallowEtagHeaderFilter.disableContentCaching(RequestUtils.getCurrentRequest());
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, @Nonnull MethodParameter returnType, @Nonnull MediaType selectedContentType,
                                  @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response) {
        long contentLength = response.getHeaders().getContentLength();
        if (contentLength < 0) {
            if (body instanceof String str) {
                contentLength = str.length();
            } else if (body instanceof Resource resource) {
                if (resource instanceof FileSystemResource || resource instanceof ByteArrayResource) {
                    try {
                        contentLength = resource.contentLength();
                        if (contentLength <= Integer.MAX_VALUE && ResponseUtils.getCurrentResponse() instanceof ContentCachingResponseWrapper wrapper) {
                            wrapper.getResponse().setContentLengthLong(contentLength);
                        }
                    } catch (IOException _) {
                        ShallowEtagHeaderFilter.disableContentCaching(RequestUtils.getCurrentRequest());
                    }
                } else {
                    ShallowEtagHeaderFilter.disableContentCaching(RequestUtils.getCurrentRequest());
                }
            } else if (body instanceof byte[] bytes) {
                contentLength = bytes.length;
            }
        }
        if (contentLength > maxContentCachingLength) ShallowEtagHeaderFilter.disableContentCaching(RequestUtils.getCurrentRequest());
        return body;
    }

}
