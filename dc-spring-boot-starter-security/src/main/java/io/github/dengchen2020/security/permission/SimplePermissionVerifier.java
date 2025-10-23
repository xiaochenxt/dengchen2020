package io.github.dengchen2020.security.permission;

import io.github.dengchen2020.core.security.principal.Authentication;

import java.util.Set;

/**
 * 简单的权限校验器
 *
 * @author xiaochen
 * @since 2024/7/22
 */
public class SimplePermissionVerifier implements PermissionVerifier {

    /**
     * 是否拥有相应的权限
     * <p>拥有其中任意一个权限即可</p>
     * @param authentication 认证信息对象
     * @param permissions 权限
     * @return true：有权限，false：无权限
     */
    @Override
    public boolean hasPermission(Authentication authentication, String... permissions) {
        if (authentication.permissions() == null) return true;
        if (authentication.permissions().isEmpty()) return false;
        Set<String> permissionSet = Set.of(permissions);
        for (String s : authentication.permissions()) {
            if (permissionSet.contains(s)) return true;
        }
        return false;
    }

}
