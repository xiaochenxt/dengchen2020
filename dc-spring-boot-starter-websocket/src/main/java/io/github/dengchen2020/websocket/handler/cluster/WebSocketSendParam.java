package io.github.dengchen2020.websocket.handler.cluster;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.web.socket.CloseStatus;

import java.io.Serial;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * 集群版websocket发送参数
 * @author xiaochen
 * @since 2023/7/18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class WebSocketSendParam implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private String[] userId;

    /**
     * 租户id
     */
    private Long tenantId;

    /**
     * 消息
     */
    private byte[] msg;

    /**
     * 0-关闭连接 1-发送给用户 2-发送给租户 3-发送给全部
     */
    private int type;

    /**
     * 1-文本消息 2-二进制消息
     */
    private int msgType = 1;

    /**
     * 关闭连接代码
     */
    private Integer closeCode;

    /**
     * 关闭连接原因
     */
    private String closeReason;

    public WebSocketSendParam() {}

    public WebSocketSendParam(String userId,String msg){
        this(new String[]{userId}, msg);
    }

    public WebSocketSendParam(String[] userId,String msg){
        this.userId = userId;
        this.msg = msg.getBytes(StandardCharsets.UTF_8);
        this.type = 1;
    }

    public WebSocketSendParam(Long tenantId,String msg){
        this.tenantId = tenantId;
        this.msg = msg.getBytes(StandardCharsets.UTF_8);
        this.type = 2;
    }

    public WebSocketSendParam(String msg){
        this.msg = msg.getBytes(StandardCharsets.UTF_8);
        this.type = 3;
    }

    public WebSocketSendParam(String userId, ByteBuffer msg){
        this(new String[]{userId}, msg);
    }

    public WebSocketSendParam(String[] userId, ByteBuffer msg){
        this.userId = userId;
        this.msg = msg.array();
        this.type = 1;
        this.msgType = 2;
    }

    public WebSocketSendParam(Long tenantId,ByteBuffer msg){
        this.tenantId = tenantId;
        this.msg = msg.array();
        this.type = 2;
        this.msgType = 2;
    }

    public WebSocketSendParam(ByteBuffer msg){
        this.msg = msg.array();
        this.type = 3;
        this.msgType = 2;
    }

    public WebSocketSendParam(String userId, CloseStatus closeStatus){
        this(new String[]{userId}, closeStatus);
    }

    public WebSocketSendParam(String[] userId, CloseStatus closeStatus){
        this.userId = userId;
        this.type = 0;
        this.closeCode = closeStatus.getCode();
        this.closeReason = closeStatus.getReason();
    }

    public WebSocketSendParam(Long tenantId, CloseStatus closeStatus){
        this.tenantId = tenantId;
        this.type = 0;
        this.closeCode = closeStatus.getCode();
        this.closeReason = closeStatus.getReason();
    }

    public String[] getUserId() {
        return userId;
    }

    public void setUserId(String[] userId) {
        this.userId = userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public Integer getCloseCode() {
        return closeCode;
    }

    public void setCloseCode(Integer closeCode) {
        this.closeCode = closeCode;
    }

    public String getCloseReason() {
        return closeReason;
    }

    public void setCloseReason(String closeReason) {
        this.closeReason = closeReason;
    }

    public CloseStatus getCloseStatus() {
        if (closeCode != null) {
            return new CloseStatus(closeCode, closeReason);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WebSocketSendParam that = (WebSocketSendParam) o;
        return type == that.type && msgType == that.msgType && Objects.deepEquals(userId, that.userId) && Objects.equals(tenantId, that.tenantId) && Objects.deepEquals(msg, that.msg) && Objects.equals(closeCode, that.closeCode) && Objects.equals(closeReason, that.closeReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(userId), tenantId, Arrays.hashCode(msg), type, msgType, closeCode, closeReason);
    }

    @Override
    public String toString() {
        return "WebSocketSendParam{" +
                "userId=" + Arrays.toString(userId) +
                ", tenantId=" + tenantId +
                ", msg=" + Arrays.toString(msg) +
                ", type=" + type +
                ", msgType=" + msgType +
                ", closeCode=" + closeCode +
                ", closeReason='" + closeReason + '\'' +
                '}';
    }
}
