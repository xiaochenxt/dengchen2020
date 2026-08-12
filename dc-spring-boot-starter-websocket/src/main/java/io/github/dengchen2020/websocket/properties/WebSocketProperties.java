package io.github.dengchen2020.websocket.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.time.Duration;

/**
 * websocket属性配置
 * @author xiaochen
 * @since 2024/6/27
 */
@ConfigurationProperties("dc.websocket")
public class WebSocketProperties {

    /**
     * 是否启用spring封装的websocket的自动配置，默认启用
     */
    private boolean enabled = true;

    /**
     * 设置允许浏览器跨域请求的源。
     * 有关格式详细信息和注意事项，请参阅CorsConfiguration.setAllowedOrigins(List)，并记住 CORS 规范不允许与 一起allowCredentials=true使用""。
     * 若要更灵活地使用原点模式，请改用setAllowedOriginPatterns。 默认情况下，不允许使用源。同时设置了 when allowedOriginPatterns ，则该属性优先于此属性。
     * 注意 当启用 SockJS 并限制源时，不允许检查请求源的传输类型（基于 Iframe 的传输）将被禁用。因此，当源受到限制时，不支持 IE 6 到 9。
     */
    private String[] allowedOrigins;

    /**
     * 除此之外 setAllowedOrigins(String...) ，它还支持更灵活的模式，用于指定允许来自浏览器的跨域请求的源。
     * 有关格式详细信息和其他注意事项，请参阅 CorsConfiguration.setAllowedOriginPatterns(List) 。 默认情况下，未设置此项。
     */
    private String[] allowedOriginPatterns;

    /**
     * 启用 SockJS 回退选项
     */
    private boolean withSockJS = false;

    /**
     * 文本消息的默认最大缓冲区大小
     */
    private DataSize maxTextMessageBufferSize = DataSize.ofKilobytes(16);

    /**
     * 二进制消息的默认最大缓冲区大小
     */
    private DataSize maxBinaryMessageBufferSize = DataSize.ofKilobytes(32);

    /**
     * 默认会话闲置超时（毫秒级）。零值或负值表示无限超时
     */
    private Duration maxSessionIdleTimeout = Duration.ofSeconds(90);

    /**
     * 异步发送消息的默认超时（毫秒级）。非正值表示无限超时
     */
    private Duration asyncSendTimeout = Duration.ofSeconds(10);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String[] getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(String[] allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String[] getAllowedOriginPatterns() {
        return allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(String[] allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    public boolean isWithSockJS() {
        return withSockJS;
    }

    public void setWithSockJS(boolean withSockJS) {
        this.withSockJS = withSockJS;
    }

    public DataSize getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    public void setMaxTextMessageBufferSize(DataSize maxTextMessageBufferSize) {
        this.maxTextMessageBufferSize = maxTextMessageBufferSize;
    }

    public DataSize getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    public void setMaxBinaryMessageBufferSize(DataSize maxBinaryMessageBufferSize) {
        this.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    }

    public Duration getMaxSessionIdleTimeout() {
        return maxSessionIdleTimeout;
    }

    public void setMaxSessionIdleTimeout(Duration maxSessionIdleTimeout) {
        this.maxSessionIdleTimeout = maxSessionIdleTimeout;
    }

    public Duration getAsyncSendTimeout() {
        return asyncSendTimeout;
    }

    public void setAsyncSendTimeout(Duration asyncSendTimeout) {
        this.asyncSendTimeout = asyncSendTimeout;
    }
}
