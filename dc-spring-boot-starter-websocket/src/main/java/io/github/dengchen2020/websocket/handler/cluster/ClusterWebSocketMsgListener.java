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
        switch (sendParam.getType()) {
            case 0 -> {
                if (sendParam.getUserId() != null) {
                    clusterSpringWebSocketHandler.closeNoPublish(sendParam.getUserId(), sendParam.getCloseStatus());
                }else if (sendParam.getTenantId() != null) {
                    clusterSpringWebSocketHandler.closeNoPublish(sendParam.getTenantId(), sendParam.getCloseStatus());
                }
            }
            case 1 -> {
                if (sendParam.getUserId() == null) {
                    log.warn("websocket发送消息异常，用户id为null，msg：{}", new String(sendParam.getMsg()));
                    return;
                }
                switch (sendParam.getMsgType()) {
                    case 1 -> clusterSpringWebSocketHandler.sendNoPublish(sendParam.getUserId(), new String(sendParam.getMsg()));
                    case 2 -> clusterSpringWebSocketHandler.sendNoPublish(sendParam.getUserId(), ByteBuffer.wrap(sendParam.getMsg()));
                }
            }
            case 2 -> {
                if (sendParam.getTenantId() == null) {
                    log.warn("websocket发送消息异常，租户id为null，msg：{}", new String(sendParam.getMsg()));
                    return;
                }
                switch (sendParam.getMsgType()) {
                    case 1 -> clusterSpringWebSocketHandler.sendNoPublish(sendParam.getTenantId(), new String(sendParam.getMsg()));
                    case 2 -> clusterSpringWebSocketHandler.sendNoPublish(sendParam.getTenantId(), ByteBuffer.wrap(sendParam.getMsg()));
                }
            }
            case 3 -> {
                switch (sendParam.getMsgType()) {
                    case 1 -> clusterSpringWebSocketHandler.sendToAllNoPublish(new String(sendParam.getMsg()));
                    case 2 -> clusterSpringWebSocketHandler.sendToAllNoPublish(ByteBuffer.wrap(sendParam.getMsg()));
                }
            }
        }
    }

}
