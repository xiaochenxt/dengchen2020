package io.github.dengchen2020.websocket.handler.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.nio.ByteBuffer;

/**
 * 集群websocket服务器通知
 *
 * @author xiaochen
 * @since 2023/7/18
 */
public class ClusterWebSocketMsgListener extends MessageListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(ClusterWebSocketMsgListener.class);

    private final ClusterDcWebSocketHandler clusterSpringWebSocketHandler;

    public ClusterWebSocketMsgListener(ClusterDcWebSocketHandler clusterSpringWebSocketHandler) {
        this.clusterSpringWebSocketHandler = clusterSpringWebSocketHandler;
    }

    public void handleMessage(WebSocketSendParam sendParam) {
        switch (sendParam.type()) {
            case 0 -> {
                if (sendParam.userId() != null) {
                    clusterSpringWebSocketHandler.closeNoPublish(sendParam.userId(), sendParam.getCloseStatus());
                }else if (sendParam.tenantId() != null) {
                    clusterSpringWebSocketHandler.closeNoPublish(sendParam.tenantId(), sendParam.getCloseStatus());
                }
            }
            case 1 -> {
                if (sendParam.userId() == null) {
                    log.warn("websocket发送消息异常，用户id为null，msg：{}", new String(sendParam.msg()));
                    return;
                }
                switch (sendParam.msgType()) {
                    case 1 -> clusterSpringWebSocketHandler.sendNoPublish(sendParam.userId(), new String(sendParam.msg()));
                    case 2 -> clusterSpringWebSocketHandler.sendNoPublish(sendParam.userId(), ByteBuffer.wrap(sendParam.msg()));
                }
            }
            case 2 -> {
                if (sendParam.tenantId() == null) {
                    log.warn("websocket发送消息异常，租户id为null，msg：{}", new String(sendParam.msg()));
                    return;
                }
                switch (sendParam.msgType()) {
                    case 1 -> clusterSpringWebSocketHandler.sendNoPublish(sendParam.tenantId(), new String(sendParam.msg()));
                    case 2 -> clusterSpringWebSocketHandler.sendNoPublish(sendParam.tenantId(), ByteBuffer.wrap(sendParam.msg()));
                }
            }
            case 3 -> {
                switch (sendParam.msgType()) {
                    case 1 -> clusterSpringWebSocketHandler.sendToAllNoPublish(new String(sendParam.msg()));
                    case 2 -> clusterSpringWebSocketHandler.sendToAllNoPublish(ByteBuffer.wrap(sendParam.msg()));
                }
            }
        }
    }

}
