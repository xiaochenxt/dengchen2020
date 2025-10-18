package io.github.dengchen2020.core.filter;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class DcShallowEtagHeaderFilter extends ShallowEtagHeaderFilter {

    private final List<PathPattern> includePatterns;

    public DcShallowEtagHeaderFilter(String[] includePatterns) {
        this.includePatterns = new ArrayList<>(includePatterns.length);
        PathPatternParser parser = PathPatternParser.defaultInstance;
        for (String pattern : includePatterns) {
            this.includePatterns.add(parser.parse(parser.initFullPathPattern(pattern)));
        }
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,@Nonnull HttpServletResponse response,@Nonnull FilterChain filterChain) throws ServletException, IOException {
        if (matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        super.doFilterInternal(request, response, filterChain);
    }

    /**
     * 检查是否匹配
     */
    public boolean matches(HttpServletRequest request) {
        if (includePatterns.isEmpty()) return false;
        PathContainer path;
        if (!ServletRequestPathUtils.hasCachedPath(request)) {
            RequestPath requestPath = ServletRequestPathUtils.parseAndCache(request);
            path = requestPath.pathWithinApplication();
        }else {
            RequestPath requestPath = ServletRequestPathUtils.getParsedRequestPath(request);
            path = requestPath.pathWithinApplication();
        }
        for (PathPattern includePattern : includePatterns) {
            if (includePattern.matches(path)) {
                return true;
            }
        }
        return false;
    }

}
