package io.github.dengchen2020.core.security.principal;

import java.util.Objects;

/**
 * 匿名身份认证
 * @author xiaochen
 * @since 2025/2/25
 */
public class AnonymousAuthentication extends Authentication {

    public static final AnonymousAuthentication INSTANCE = new AnonymousAuthentication();

    public AnonymousAuthentication() {this.name = "anonymousUser";}

    public AnonymousAuthentication(String name) {
        this.name = name == null ? "anonymousUser" : name;
    }

    private final String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AnonymousAuthentication that = (AnonymousAuthentication) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    @Override
    public String toString() {
        return "AnonymousAuthentication{" +
                "name='" + name + '\'' +
                '}';
    }
}
