package io.github.dengchen2020.websocket.config;

import io.github.dengchen2020.websocket.annotation.WebSocketMapping;
import io.github.dengchen2020.websocket.properties.WebSocketProperties;
import jakarta.websocket.server.ServerEndpoint;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ClassUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.HashSet;
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

    private final ObjectProvider<WebSocketHandler> webSocketHandlers;

    private final ObjectProvider<HandshakeHandler> handshakeHandler;

    private final ObjectProvider<HandshakeInterceptor> handshakeInterceptors;

    private final WebSocketProperties webSocketProperties;

    SpringWebSocketAutoConfiguration(ObjectProvider<WebSocketHandler> webSocketHandlers, ObjectProvider<HandshakeHandler> handshakeHandler, ObjectProvider<HandshakeInterceptor> handshakeInterceptors, WebSocketProperties webSocketProperties) {
        this.webSocketHandlers = webSocketHandlers;
        this.handshakeHandler = handshakeHandler;
        this.handshakeInterceptors = handshakeInterceptors;
        this.webSocketProperties = webSocketProperties;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        Set<String> handlerMappings = new HashSet<>();
        for (WebSocketHandler handle : webSocketHandlers) {
            if (handle.getClass().getAnnotation(ServerEndpoint.class) != null) throw new IllegalArgumentException("无需添加@ServerEndpoint注解，" + handle.getClass().getName());
            WebSocketMapping webSocketMapping = handle.getClass().getAnnotation(WebSocketMapping.class);
            if (webSocketMapping == null || webSocketMapping.value().isBlank()) continue;
            var mapping = webSocketMapping.value();
            if (handlerMappings.contains(mapping)) throw new IllegalArgumentException(handle.getClass().getName()+" 配置错误，原因是重复映射："+ mapping);
            handlerMappings.add(mapping);
            WebSocketHandlerRegistration registration = registry.addHandler(handle, mapping);
            if(webSocketProperties.getAllowedOrigins() != null) registration.setAllowedOrigins(webSocketProperties.getAllowedOrigins());
            if(webSocketProperties.getAllowedOriginPatterns() != null) registration.setAllowedOriginPatterns(webSocketProperties.getAllowedOriginPatterns());
            if(webSocketProperties.getAllowedOrigins() == null && webSocketProperties.getAllowedOriginPatterns() == null) registration.setAllowedOriginPatterns("*");
            if(webSocketProperties.isWithSockJS()) registration.withSockJS();
            var handshake = handshakeHandler.getIfAvailable();
            if(handshake != null) registration.setHandshakeHandler(handshake);
            handshakeInterceptors.forEach(registration::addInterceptors);
        }
    }

    /**
     * 配置websocket容器
     */
    @Lazy(false)
    @ConditionalOnMissingBean
    @Bean
    ServletServerContainerFactoryBean serverContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize((int) webSocketProperties.getMaxTextMessageBufferSize().toBytes());
        container.setMaxBinaryMessageBufferSize((int) webSocketProperties.getMaxBinaryMessageBufferSize().toBytes());
        container.setMaxSessionIdleTimeout(webSocketProperties.getMaxSessionIdleTimeout().toMillis());
        container.setAsyncSendTimeout(webSocketProperties.getAsyncSendTimeout().toMillis());
        boolean jettyWsPresent = ClassUtils.isPresent("org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServerContainer", SpringWebSocketAutoConfiguration.class.getClassLoader());
        // jetty未适配这种配置方式
        if (jettyWsPresent) throw new IllegalStateException("Jetty WebSocket is not supported");
        return container;
    }

}
