package io.github.dengchen2020.websocket.handler.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.web.socket.CloseStatus;

import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 集群版websocket发送参数
 *
 * @param userId      用户id
 * @param tenantId    租户id
 * @param msg         消息
 * @param type        0-关闭连接 1-发送给用户 2-发送给租户 3-发送给全部
 * @param msgType     1-文本消息 2-二进制消息
 * @param closeCode   关闭连接代码
 * @param closeReason 关闭连接原因
 * @author xiaochen
 * @since 2023/7/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record WebSocketSendParam(String[] userId, Long tenantId, byte[] msg, int type, int msgType, Integer closeCode,
                                 String closeReason) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public WebSocketSendParam(String userId, String msg){
        this(new String[]{userId}, msg);
    }

    public WebSocketSendParam(String[] userId,String msg){
        this(userId, null, msg.getBytes(StandardCharsets.UTF_8), 1, 1, null, null);
    }

    public WebSocketSendParam(Long tenantId,String msg){
        this(null, tenantId, msg.getBytes(StandardCharsets.UTF_8), 2, 1, null, null);
    }

    public WebSocketSendParam(String msg){
        this(null, null, msg.getBytes(StandardCharsets.UTF_8), 3, 1, null, null);
    }

    public WebSocketSendParam(String userId, ByteBuffer msg){
        this(new String[]{userId}, msg);
    }

    public WebSocketSendParam(String[] userId, ByteBuffer msg){
        this(userId, null, msg.array(), 1, 2, null, null);
    }

    public WebSocketSendParam(Long tenantId,ByteBuffer msg){
        this(null, tenantId, msg.array(), 2, 2, null, null);
    }

    public WebSocketSendParam(ByteBuffer msg){
        this(null, null, msg.array(), 3, 2, null, null);
    }

    public WebSocketSendParam(String userId, CloseStatus closeStatus){
        this(new String[]{userId}, closeStatus);
    }

    public WebSocketSendParam(String[] userId, CloseStatus closeStatus){
        this(userId, null, null, 0, 1, closeStatus.getCode(), closeStatus.getReason());
    }

    public WebSocketSendParam(Long tenantId, CloseStatus closeStatus){
        this(null, tenantId, null, 0, 1, closeStatus.getCode(), closeStatus.getReason());
    }

    public CloseStatus getCloseStatus() {
        if (closeCode != null) return new CloseStatus(closeCode, closeReason);
        return CloseStatus.NORMAL.withReason("未知");
    }

}
