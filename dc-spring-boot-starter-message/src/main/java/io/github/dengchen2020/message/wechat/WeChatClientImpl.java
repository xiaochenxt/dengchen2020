package io.github.dengchen2020.message.wechat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.dengchen2020.core.utils.JsonUtils;
import io.github.dengchen2020.core.utils.RestClientUtils;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;

/**
 * 微信webhook推送
 * @author xiaochen
 * @since 2025/4/7
 */
@NullMarked
public class WeChatClientImpl implements WeChatClient {

    private static final Logger log = LoggerFactory.getLogger(WeChatClientImpl.class);

    private final AsyncTaskExecutor executor;

    private static final AsyncTaskExecutor defaultExecutor = new VirtualThreadTaskExecutor("wechat-");

    private final String webhook;

    public WeChatClientImpl(String webhook) {
        this(webhook, defaultExecutor);
    }

    public WeChatClientImpl(String webhook, AsyncTaskExecutor executor) {
        this.webhook = webhook;
        this.executor = executor;
    }

    @Override
    public void send(Message message) {
        send(message, webhook);
    }

    @Override
    public void send(Message message, String webhook) {
        if (!StringUtils.hasText(webhook)) {
            log.warn("未配置微信webhook");
            return;
        }
        executor.execute(() -> sendSync(message, webhook));
    }

    public boolean sendSync(Message message, String webhook) {
        ObjectNode params = JsonUtils.createObjectNode();
        try {
            params.put("msgtype", message.type());
            params.putPOJO(message.type(), message);
            JsonNode res = RestClientUtils.post().uri(webhook).contentType(MediaType.APPLICATION_JSON).body(params).retrieve().body(JsonNode.class);
            if (res.path("errcode").asInt() != 0) {
                log.error("微信机器人发送信息失败，参数：{}，webhook：{}，响应信息：{}", params, webhook, res);
                return false;
            }
        } catch (Exception e) {
            log.error("微信机器人发送信息失败，参数：{}，webhook：{}，异常信息：", params, webhook, e);
            return false;
        }
        return true;
    }

    @Override
    public String upload(Resource resource, String type, String key) {
        LinkedMultiValueMap<String, Object> form = new LinkedMultiValueMap<>(1);
        form.add("media", resource);
        JsonNode res = RestClientUtils.post().uri("https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media?key=" + key + "&type=" +type)
                .body(form).retrieve().body(JsonNode.class);
        int errcode = res.get("errcode").asInt();
        if (errcode != 0) {
            log.error("微信机器人上传文件失败：{}", res);
            return null;
        }
        return res.path("media_id").asText();
    }
}
