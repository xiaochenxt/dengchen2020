package io.github.dengchen2020.core.security.principal;

import java.io.Serial;
import java.io.Serializable;

/**
 * 匿名用户认证信息
 *
 * @author xiaochen
 * @since 2025/2/25
 */
public record AnonymousAuthentication(String name) implements Authentication, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
    public String getName() {
        return name;
    }

}
