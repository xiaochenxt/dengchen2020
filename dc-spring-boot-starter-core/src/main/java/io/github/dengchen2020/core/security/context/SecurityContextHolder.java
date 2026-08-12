package io.github.dengchen2020.core.security.context;

import io.github.dengchen2020.core.security.principal.Authentication;

/**
 * 认证信息上下文管理
 * @author xiaochen
 * @since 2024/4/25
 */
public class SecurityContextHolder {

    private final static ThreadLocal<Authentication> currentAuthentication = new ThreadLocal<>();

    /**
     * 保存认证信息
     * @param authentication 认证信息
     */
    public static void setAuthentication(Authentication authentication) {
        if (authentication == null) {
            currentAuthentication.remove();
        }else {
            currentAuthentication.set(authentication);
        }
    }

    /**
     * 获取认证信息
     * @return 认证信息
     */
    public static Authentication getAuthentication() {
        return currentAuthentication.get();
    }

    /**
     * 清除认证信息上下文
     */
    public static void clear() {
        currentAuthentication.remove();
    }

}
