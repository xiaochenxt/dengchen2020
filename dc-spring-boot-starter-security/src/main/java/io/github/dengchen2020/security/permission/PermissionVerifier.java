package io.github.dengchen2020.security.permission;

import io.github.dengchen2020.core.security.principal.Authentication;

/**
 * 权限校验基础接口
 * @author xiaochen
 * @since 2024/7/22
 */
public interface PermissionVerifier {

    /**
     * 是否拥有指定的权限
     * @param authentication 认证信息对象
     * @return true：有权限，false：没权限
     */
    boolean hasPermission(Authentication authentication, String... permissions);

}
