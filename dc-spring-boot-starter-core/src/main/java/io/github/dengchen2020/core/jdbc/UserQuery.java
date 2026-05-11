package io.github.dengchen2020.core.jdbc;

import io.github.dengchen2020.core.security.context.SecurityContextHolder;

import java.util.Objects;

/**
 * 用户查询
 * @author xiaochen
 * @since 2025/9/23
 */
public interface UserQuery<ID> {

    ID getUserId();

    default boolean isSameUser() {
        var authentication = SecurityContextHolder.getAuthentication();
        if (authentication == null) return false;
        return Objects.equals(getUserId().toString(), authentication.userId());
    }

}
