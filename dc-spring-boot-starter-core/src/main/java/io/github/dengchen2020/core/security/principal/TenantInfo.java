package io.github.dengchen2020.core.security.principal;

/**
 * 租户信息
 * @author xiaochen
 * @since 2025/11/28
 */
public interface TenantInfo extends Authentication {

    /**
     * @return 租户id
     */
    Long tenantId();

}
