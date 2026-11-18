package io.github.dengchen2020.message.wechat;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.github.dengchen2020.core.utils.jsonb.JsonbUtils;
import io.github.dengchen2020.core.utils.RestClientUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;

import java.util.HashMap;

/**
 * 微信webhook推送
 * @author xiaochen
 * @since 2025/4/7
 */
@NullMarked
public class WeChatClientImpl implements WeChatClient {

    private static final Logger log = LoggerFactory.getLogger(WeChatClientImpl.class);

    private static final JsonType<Response> responseType = JsonbUtils.type(Response.class);

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
        var params = "";
        try {
            var body = new HashMap<String, Object>(4);
            body.put("msgtype", message.type());
            body.put(message.type(), message);
            params = JsonbUtils.toJson(body);
            var resStr = RestClientUtils.post().uri(webhook).contentType(MediaType.APPLICATION_JSON)
                    .body(params).retrieve().body(String.class);
            var res = responseType.fromJson(resStr);
            if (res.errcode() != 0) {
                log.error("微信机器人发送信息失败，参数：{}，webhook：{}，响应信息：{}", params, webhook, resStr);
                return false;
            }
        } catch (Exception e) {
            log.error("微信机器人发送信息失败，参数：{}，webhook：{}，异常信息：", params, webhook, e);
            return false;
        }
        return true;
    }

    @Override
    public @Nullable String upload(Resource resource, String type, String key) {
        var form = new LinkedMultiValueMap<String, Object>(1);
        form.add("media", resource);
        var resStr = RestClientUtils.post().uri("https://qyapi.weixin.qq.com/cgi-bin/webhook/upload_media?key=" + key + "&type=" + type)
                .body(form).retrieve().body(String.class);
        var res = responseType.fromJson(resStr);
        if (res.errcode() != 0 || res.media_id() == null) {
            log.error("微信机器人上传文件失败：{}", resStr);
            return null;
        }
        return res.media_id();
    }

    /**
     * 微信webhook响应
     */
    @Json
    record Response(int errcode, String errmsg, @Nullable String media_id) {
    }
}
