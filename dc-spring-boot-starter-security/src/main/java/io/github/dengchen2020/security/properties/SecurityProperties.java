package io.github.dengchen2020.security.properties;

import io.github.dengchen2020.core.security.principal.Authentication;
import io.github.dengchen2020.security.permission.PermissionVerifier;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * token配置信息
 *
 * @author xiaochen
 * @since 2019/10/8 13:06
 */
@ConfigurationProperties(prefix = "dc.security")
public class SecurityProperties {

    /**
     * Token认证信息实例类型
     */
    private Class<? extends Authentication> authenticationType;

    /**
     * 有状态Token认证
     */
    private Token token = new Token();

    /**
     * 无状态Token认证
     */
    private JWT jwt = new JWT();

    /**
     * 简单的有状态Token认证，单个用户只能登录一个设备，存储结构简单
     */
    private SimpleToken simpleToken = new SimpleToken();

    /**
     * 访问资源配置
     */
    private Resource resource = new Resource();

    /**
     * 权限校验配置
     */
    private Permission permission = new Permission();

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

    public static class JWT {

        /**
         * 密钥
         */
        private String secret;

        /**
         * 请求token有效期
         */
        private Duration expireIn = Duration.ofSeconds(1800);

        /**
         * 刷新token有效期
         */
        private Duration refreshExpireIn = Duration.ofDays(30);

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getExpireIn() {
            return expireIn;
        }

        public void setExpireIn(Duration expireIn) {
            this.expireIn = expireIn;
        }

        public Duration getRefreshExpireIn() {
            return refreshExpireIn;
        }

        public void setRefreshExpireIn(Duration refreshExpireIn) {
            this.refreshExpireIn = refreshExpireIn;
        }
    }

    public static class Token {

        /**
         * 请求token有效期
         */
        private Duration expireIn;

        /**
         * 单个用户最多允许同时在线的设备数
         */
        private int maxOnlineNum = 1;

        /**
         * 设备，客户端类型
         */
        private String device;

        /**
         * 是否自动续期
         */
        private boolean autorenewal = false;

        /**
         * 自动续期的时间阈值，token剩余有效时间小于该值时自动续期
         */
        private int autorenewalSeconds = 180;

        public Duration getExpireIn() {
            return expireIn;
        }

        public void setExpireIn(Duration expireIn) {
            this.expireIn = expireIn;
        }

        public int getMaxOnlineNum() {
            return maxOnlineNum;
        }

        public void setMaxOnlineNum(int maxOnlineNum) {
            this.maxOnlineNum = maxOnlineNum;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public boolean isAutorenewal() {
            return autorenewal;
        }

        public void setAutorenewal(boolean autorenewal) {
            this.autorenewal = autorenewal;
        }

        public int getAutorenewalSeconds() {
            return autorenewalSeconds;
        }

        public void setAutorenewalSeconds(int autorenewalSeconds) {
            this.autorenewalSeconds = autorenewalSeconds;
        }
    }

    public static class SimpleToken {

        /**
         * 请求token有效期
         */
        private Duration expireIn;

        /**
         * 设备，客户端类型
         */
        private String device;

        /**
         * 是否自动续期
         */
        private boolean autorenewal = false;

        /**
         * 自动续期的时间阈值，token剩余有效时间小于该值时自动续期
         */
        private int autorenewalSeconds = 180;

        public Duration getExpireIn() {
            return expireIn;
        }

        public void setExpireIn(Duration expireIn) {
            this.expireIn = expireIn;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public boolean isAutorenewal() {
            return autorenewal;
        }

        public void setAutorenewal(boolean autorenewal) {
            this.autorenewal = autorenewal;
        }

        public int getAutorenewalSeconds() {
            return autorenewalSeconds;
        }

        public void setAutorenewalSeconds(int autorenewalSeconds) {
            this.autorenewalSeconds = autorenewalSeconds;
        }
    }

    public Class<? extends Authentication> getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(Class<? extends Authentication> authenticationType) {
        this.authenticationType = authenticationType;
    }

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

    public JWT getJwt() {
        return jwt;
    }

    public void setJwt(JWT jwt) {
        this.jwt = jwt;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public SimpleToken getSimpleToken() {
        return simpleToken;
    }

    public void setSimpleToken(SimpleToken simpleToken) {
        this.simpleToken = simpleToken;
    }

}
