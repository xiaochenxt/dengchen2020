package io.github.dengchen2020.core.security.principal;

import java.io.Serializable;
import java.util.Set;

/**
 * 匿名用户认证信息
 *
 * @author xiaochen
 * @since 2025/2/25
 */
public record AnonymousAuthentication(String name) implements Authentication, Serializable {

    public static final AnonymousAuthentication INSTANCE = new AnonymousAuthentication();

    public AnonymousAuthentication() {
        this("anonymousUser");
    }

    public AnonymousAuthentication(String name) {
        this.name = name == null ? "anonymousUser" : name;
    }

    @Override
    public String userId() {
        return null;
    }

    @Override
    public Long tenantId() {
        return null;
    }

    @Override
    public Set<String> permissions() {
        return null;
    }

    @Override
    public String getName() {
        return name;
    }

}
