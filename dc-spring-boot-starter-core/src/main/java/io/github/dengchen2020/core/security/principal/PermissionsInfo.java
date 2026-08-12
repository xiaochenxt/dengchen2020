package io.github.dengchen2020.core.security.principal;

import java.util.Set;

/**
 * 权限信息
 * @author xiaochen
 * @since 2025/11/28
 */
public interface PermissionsInfo extends Authentication {

    /**
     * @return 权限
     */
    Set<String> permissions();

}
