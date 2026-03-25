package io.github.dengchen2020.core.filter;

import io.github.dengchen2020.core.web.mvc.StaticResourceServlet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.util.ServletRequestPathUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 扩展 {@link ShallowEtagHeaderFilter}
 * @author xiaochen
 * @since 2025/4/22
 */
@NullMarked
public class DcShallowEtagHeaderFilter extends ShallowEtagHeaderFilter {

    private final List<PathPattern> ignorePath;

    public DcShallowEtagHeaderFilter(String[] ignorePath) {
        this.ignorePath = new ArrayList<>(ignorePath.length);
        PathPatternParser parser = PathPatternParser.defaultInstance;
        for (String pattern : ignorePath) {
            this.ignorePath.add(parser.parse(parser.initFullPathPattern(pattern)));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var servletName = request.getHttpServletMapping().getServletName();
        if (StaticResourceServlet.SERVLET_NAME.equals(servletName)) return true; // 静态资源使用Last-Modified就行了，不需要浪费CPU和内存额外使用Etag
        if (ignorePath.isEmpty()) return false;
        PathContainer path;
        if (!ServletRequestPathUtils.hasCachedPath(request)) {
            RequestPath requestPath = ServletRequestPathUtils.parseAndCache(request);
            path = requestPath.pathWithinApplication();
        }else {
            RequestPath requestPath = ServletRequestPathUtils.getParsedRequestPath(request);
            path = requestPath.pathWithinApplication();
        }
        for (PathPattern pathPattern : ignorePath) {
            if (pathPattern.matches(path)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        super.doFilterInternal(request, response, filterChain);
    }

}
