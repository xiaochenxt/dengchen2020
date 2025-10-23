package io.github.dengchen2020.core.security.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.security.Principal;
import java.util.Set;

/**
 * 认证信息
 * @author xiaochen
 * @since 2024/4/28
 */
public interface Authentication extends Principal {

    /**
     * @return 用户id，最好是纯数字，不能包含-，特殊字符均不能有
     */
    String userId();

    /**
     * @return 租户id
     */
    Long tenantId();

    /**
     * @return 权限
     */
    Set<String> permissions();

    /**
     * 返回用户的唯一标识
     * @return 用户的唯一标识
     */
    @JsonIgnore
    @Override
    default String getName() {
        return userId();
    }

}
