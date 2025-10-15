package io.github.dengchen2020.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * CORS配置
 * @author xiaochen
 * @since 2024/7/5
 */
@ConfigurationProperties("dc.cors")
public class XcCorsProperties {

    /**
     * 是否启用CORS过滤器，默认启用，非特殊需求不建议关闭
     */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 允许的请求来源
     */
    private List<String> allowedOrigins;

    /**
     * 允许的请求来源规则，默认：*
     */
    private String allowedOriginPatterns;

    /**
     * 允许的请求方法，默认："GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "QUERY"
     */
    private List<String> allowedMethods;

    /**
     * 允许的请求头部，默认：*
     */
    private List<String> allowedHeaders;

    /**
     * 允许客户端访问的响应头部
     */
    private List<String> exposedHeaders;

    /**
     * 是否支持用户凭据，如cookie，token，默认：true
     */
    private Boolean allowCredentials;

    /**
     * 是否允许https公共网站向http私有网站或localhost网址发出请求。不安全，默认：false
     * 专用网络请求是指其目标服务器的 IP 地址比获取请求发起者时的 IP 地址更私密的请求。例如，从公共网站 （https://example.com） 向私有网站 （https://router.local） 发出的请求，或从私有网站向 localhost 发出的请求。
     */
    private Boolean allowPrivateNetwork;

    /**
     * 配置客户端可以缓存预检请求的响应的时间长度（作为持续时间）
     */
    private Duration maxAge;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(String allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public Boolean getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Boolean getAllowPrivateNetwork() {
        return allowPrivateNetwork;
    }

    public void setAllowPrivateNetwork(Boolean allowPrivateNetwork) {
        this.allowPrivateNetwork = allowPrivateNetwork;
    }

    public Duration getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Duration maxAge) {
        this.maxAge = maxAge;
    }
}
