package io.github.dengchen2020.core.security.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.Objects;
import java.util.Set;

/**
 * 基础认证信息
 * @author xiaochen
 * @since 2024/4/28
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Authentication implements Principal, Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public Authentication() {}

    public Authentication(String userId) {
        this.userId = userId;
    }

    public Authentication(String userId, Long tenantId) {
        this.userId = userId;
        this.tenantId = tenantId;
    }

    private String userId;

    private Long tenantId;

    private Set<String> permissions;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * 最好是纯数字
     * <p>不能包含-，特殊字符均不能有</p>
     * @param userId
     * @return
     */
    public Authentication userId(String userId) {
        this.userId = userId;
        return this;
    }

    public Authentication userId(long userId) {
        this.userId = String.valueOf(userId);
        return this;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Authentication tenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Authentication permissions(Set<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * 返回用户的唯一标识
     * @return 用户的唯一标识
     */
    @JsonIgnore
    @Override
    public String getName() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Authentication that = (Authentication) o;
        return Objects.equals(userId, that.userId) && Objects.equals(tenantId, that.tenantId) && Objects.equals(permissions, that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, tenantId, permissions);
    }

    @Override
    public String toString() {
        return "Authentication{" +
                "userId='" + userId + '\'' +
                ", tenantId=" + tenantId +
                ", permissions=" + permissions +
                '}';
    }
}
