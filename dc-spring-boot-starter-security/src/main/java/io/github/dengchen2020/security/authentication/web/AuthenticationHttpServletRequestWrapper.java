package io.github.dengchen2020.security.authentication.web;

import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.Authentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * 包装HttpServletRequest对象
 *
 * @author xiaochen
 */
public class AuthenticationHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 包装给定的HttpServletRequest对象
     */
    public AuthenticationHttpServletRequestWrapper(final HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getRemoteUser() {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        if (authentication != null) return authentication.toString();
        return null;
    }

    @Override
    public java.security.Principal getUserPrincipal() {
        return SecurityContextHolder.getAuthentication();
    }

}
