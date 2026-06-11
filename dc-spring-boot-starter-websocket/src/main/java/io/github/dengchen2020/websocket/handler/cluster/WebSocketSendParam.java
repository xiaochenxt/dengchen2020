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
 * @param type        0-关闭连接 1-发送给用户 2-发送给租户下所有用户 3-发送给全部用户
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

    private WebSocketSendParam(String[] userId, byte[] msg, int type, int msgType) {
        this(userId, null, msg, type, msgType, null, null);
    }

    private WebSocketSendParam(Long tenantId, byte[] msg, int type, int msgType) {
        this(null, tenantId, msg, type, msgType, null, null);
    }

    private WebSocketSendParam(byte[] msg, int type, int msgType) {
        this((Long) null, msg, type, msgType);
    }

    /**
     * 文本消息
     */
    static final int MSG_TYPE_TEXT = 1;
    /**
     * 二进制消息
     */
    static final int MSG_TYPE_BINARY = 2;

    /**
     * 关闭连接
     */
    static final int TYPE_CLOSE = 0;
    /**
     * 发送给用户
     */
    static final int TYPE_USER = 1;
    /**
     * 发送给租户下所有用户
     */
    static final int TYPE_TENANT = 2;
    /**
     * 发送给全部用户
     */
    static final int TYPE_ALL = 3;

    /**
     * 构建发送给指定用户的文本消息参数
     * @param userId 用户id
     * @param msg    文本消息
     */
    public static WebSocketSendParam userText(String userId, String msg) {
        return userText(new String[]{userId}, msg);
    }

    /**
     * 构建发送给指定用户的文本消息参数
     * @param userId 用户id
     * @param msg    文本消息
     */
    public static WebSocketSendParam userText(String[] userId, String msg) {
        return new WebSocketSendParam(userId, msg.getBytes(StandardCharsets.UTF_8), TYPE_USER, MSG_TYPE_TEXT);
    }

    /**
     * 构建发送给指定租户下所有用户的文本消息参数
     * @param tenantId 租户id
     * @param msg    文本消息
     */
    public static WebSocketSendParam tenantText(Long tenantId,String msg){
        return new WebSocketSendParam(tenantId, msg.getBytes(StandardCharsets.UTF_8), TYPE_TENANT, MSG_TYPE_TEXT);
    }

    /**
     * 构建发送给全部用户的文本消息参数
     * @param msg    文本消息
     */
    public static WebSocketSendParam allText(String msg){
        return new WebSocketSendParam(msg.getBytes(StandardCharsets.UTF_8), TYPE_ALL, MSG_TYPE_TEXT);
    }

    /**
     * 构建发送给指定用户的二进制消息参数
     * @param userId 用户id
     * @param msg    二进制消息
     */
    public static WebSocketSendParam userBinary(String userId, ByteBuffer msg){
        return userBinary(new String[]{userId}, msg);
    }

    /**
     * 构建发送给指定用户的二进制消息参数
     * @param userId 用户id
     * @param msg    二进制消息
     */
    public static WebSocketSendParam userBinary(String[] userId, ByteBuffer msg){
        return new WebSocketSendParam(userId, msg.array(), TYPE_USER, MSG_TYPE_BINARY);
    }

    /**
     * 构建发送给指定租户下所有用户的二进制消息参数
     * @param tenantId 租户id
     * @param msg    二进制消息
     */
    public static WebSocketSendParam tenantBinary(Long tenantId,ByteBuffer msg){
        return new WebSocketSendParam(tenantId, msg.array(), TYPE_TENANT, MSG_TYPE_BINARY);
    }

    /**
     * 构建发送给全部用户的二进制消息参数
     * @param msg    二进制消息
     */
    public static WebSocketSendParam allBinary(ByteBuffer msg){
        return new WebSocketSendParam(msg.array(), TYPE_ALL, MSG_TYPE_BINARY);
    }

    /**
     * 构建关闭连接参数
     * @param userId 用户id
     * @param closeStatus 关闭连接原因
     */
    public static WebSocketSendParam closeStatus(String userId, CloseStatus closeStatus){
        return closeStatus(new String[]{userId}, closeStatus);
    }

    /**
     * 构建关闭连接参数
     * @param userId 用户id
     * @param closeStatus 关闭连接原因
     */
    public static WebSocketSendParam closeStatus(String[] userId, CloseStatus closeStatus){
        return new WebSocketSendParam(userId, null, null, TYPE_CLOSE, MSG_TYPE_TEXT, closeStatus.getCode(), closeStatus.getReason());
    }

    /**
     * 构建关闭连接参数
     * @param tenantId 租户id
     * @param closeStatus 关闭连接原因
     */
    public static WebSocketSendParam closeStatus(Long tenantId, CloseStatus closeStatus){
        return new WebSocketSendParam(null, tenantId, null, TYPE_CLOSE, MSG_TYPE_TEXT, closeStatus.getCode(), closeStatus.getReason());
    }

}
