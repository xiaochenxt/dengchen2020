package io.github.dengchen2020.core.security.principal;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 用户认证信息
 *
 * @author xiaochen
 * @since 2024/4/28
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SimpleUserAuthentication(String userId, Long tenantId, Set<String> permissions, Map<String, Object> info) implements Authentication, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public SimpleUserAuthentication(String userId, Map<String, Object> info) {
        this(userId, null, null, info);
    }

    public SimpleUserAuthentication(String userId) {
        this(userId, null, null, null);
    }

    public SimpleUserAuthentication(String userId, Long tenantId) {
        this(userId, tenantId, null, null);
    }

    public SimpleUserAuthentication(String userId, Set<String> permissions) {
        this(userId, null, permissions, null);
    }

}
