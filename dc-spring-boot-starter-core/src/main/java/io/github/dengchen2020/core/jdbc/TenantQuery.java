package io.github.dengchen2020.core.jdbc;

import io.github.dengchen2020.core.security.context.SecurityContextHolder;
import io.github.dengchen2020.core.security.principal.TenantInfo;

import java.util.Objects;

/**
 * 租户查询
 * @author xiaochen
 * @since 2025/9/23
 */
public interface TenantQuery {

    Long getTenantId();

    default boolean isSameTenant() {
        var authentication = SecurityContextHolder.getAuthentication();
        if (!(authentication instanceof TenantInfo tenantInfo)) return false;
        return Objects.equals(getTenantId(), tenantInfo.tenantId());
    }

}
