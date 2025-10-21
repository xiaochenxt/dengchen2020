package io.github.dengchen2020.websocket.config;

import io.github.dengchen2020.websocket.annotation.WebSocketMapping;
import io.github.dengchen2020.websocket.properties.WebSocketProperties;
import jakarta.annotation.Nonnull;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.Nullable;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * spring封装的websocket自动配置
 * @author xiaochen
 * @since 2024/6/27
 */
@ConditionalOnProperty(value = "dc.websocket.enabled", matchIfMissing = true, havingValue = "true")
@EnableConfigurationProperties(WebSocketProperties.class)
@EnableWebSocket
@ConditionalOnClass(WebSocketHandler.class)
@Configuration(proxyBeanMethods = false)
public final class SpringWebSocketAutoConfiguration implements WebSocketConfigurer {

    private final List<WebSocketHandler> webSocketHandlers;

    private final HandshakeHandler handshakeHandler;

    private final HandshakeInterceptor[] handshakeInterceptors;

    private final WebSocketProperties webSocketProperties;

    SpringWebSocketAutoConfiguration(@Nullable List<WebSocketHandler> webSocketHandlers, @Nullable HandshakeHandler handshakeHandler, @Nullable HandshakeInterceptor[] handshakeInterceptors, WebSocketProperties webSocketProperties) {
        this.webSocketHandlers = webSocketHandlers;
        this.handshakeHandler = handshakeHandler;
        this.handshakeInterceptors = handshakeInterceptors;
        this.webSocketProperties = webSocketProperties;
    }

    @Override
    public void registerWebSocketHandlers(@Nonnull WebSocketHandlerRegistry registry) {
        if (webSocketHandlers == null) return;
        Set<String> handlerMappings = new HashSet<>();
        for (WebSocketHandler handle : webSocketHandlers) {
            if (handle.getClass().getAnnotation(ServerEndpoint.class) != null) throw new RuntimeException("无需添加@ServerEndpoint注解，" + handle.getClass().getName());
            WebSocketMapping webSocketMapping = handle.getClass().getAnnotation(WebSocketMapping.class);
            if (webSocketMapping == null || webSocketMapping.value().length == 0) continue;
            for (String mapping : webSocketMapping.value()) {
                if (handlerMappings.contains(mapping)) throw new RuntimeException(handle.getClass().getName()+" 配置错误，原因是重复映射："+ mapping);
            }
            handlerMappings.addAll(Set.of(webSocketMapping.value()));
            WebSocketHandlerRegistration registration = registry.addHandler(handle, webSocketMapping.value());
            if(webSocketProperties.getAllowedOrigins() != null) registration.setAllowedOrigins(webSocketProperties.getAllowedOrigins());
            if(webSocketProperties.getAllowedOriginPatterns() != null) registration.setAllowedOriginPatterns(webSocketProperties.getAllowedOriginPatterns());
            if(webSocketProperties.getAllowedOrigins() == null && webSocketProperties.getAllowedOriginPatterns() == null) registration.setAllowedOriginPatterns("*");
            if(webSocketProperties.isWithSockJS()) registration.withSockJS();
            if(handshakeHandler != null) registration.setHandshakeHandler(handshakeHandler);
            if(handshakeInterceptors != null) registration.addInterceptors(handshakeInterceptors);
        }
    }

}
