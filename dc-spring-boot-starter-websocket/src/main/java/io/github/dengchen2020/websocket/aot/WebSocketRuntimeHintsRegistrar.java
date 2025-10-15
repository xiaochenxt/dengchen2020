package io.github.dengchen2020.websocket.aot;

import io.github.dengchen2020.aot.utils.AotUtils;
import io.github.dengchen2020.websocket.handler.cluster.ClusterWebSocketMsgListener;
import io.github.dengchen2020.websocket.handler.cluster.WebSocketSendParam;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * @author xiaochen
 * @since 2025/5/23
 */
public class WebSocketRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = new AotUtils(hints, classLoader);
        aotUtils.registerReflection(WebSocketSendParam.class, HandshakeHandler.class, HandshakeInterceptor.class);
        if (aotUtils.isPresent("org.springframework.data.redis.listener.adapter.MessageListenerAdapter")) {
            aotUtils.registerReflection(new MemberCategory[]{MemberCategory.INVOKE_DECLARED_METHODS}, ClusterWebSocketMsgListener.class);
        }
    }
}
