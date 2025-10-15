package io.github.dengchen2020.security.properties;

import io.github.dengchen2020.security.permission.PermissionVerifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * token配置信息
 *
 * @author xiaochen
 * @since 2019/10/8 13:06
 */
@Validated
@ConfigurationProperties(prefix = "dc.security")
public class SecurityProperties {

    /**
     * 访问资源配置
     */
    private Resource resource = new Resource();

    /**
     * 权限校验配置
     */
    private Permission permission = new Permission();

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public static class Resource {
        /**
         * 无需Token认证的访问资源路径
         */
        private List<String> permitPath = new ArrayList<>();

        public List<String> getPermitPath() {
            return permitPath;
        }

        public void setPermitPath(List<String> permitPath) {
            this.permitPath = permitPath;
        }
    }

    public static class Permission {

        /**
         * 是否开启权限校验，默认不开启，实际开发中权限校验可能比较复杂
         * 可通过实现{@link PermissionVerifier}完成复杂的权限校验
         */
        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

}
