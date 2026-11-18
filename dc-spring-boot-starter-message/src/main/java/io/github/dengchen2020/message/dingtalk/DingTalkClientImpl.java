package io.github.dengchen2020.message.dingtalk;

import io.avaje.jsonb.Json;
import io.avaje.jsonb.JsonType;
import io.github.dengchen2020.core.utils.RestClientUtils;
import io.github.dengchen2020.core.utils.jsonb.JsonbUtils;
import io.github.dengchen2020.core.utils.sign.HMACUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

/**
 * 钉钉webhook推送
 *
 * @author xiaochen
 * @since 2023/3/11
 */
@NullMarked
public class DingTalkClientImpl implements DingTalkClient {

    private static final Logger log = LoggerFactory.getLogger(DingTalkClientImpl.class);

    private static final JsonType<Response> responseType = JsonbUtils.type(Response.class);

    private final AsyncTaskExecutor executor;

    private static final AsyncTaskExecutor defaultExecutor = new VirtualThreadTaskExecutor("dingTalk-");

    private final String webhook;

    @Nullable
    private final String secret;

    public DingTalkClientImpl(String webhook) {
        this(webhook, null, defaultExecutor);
    }

    public DingTalkClientImpl(String webhook,@Nullable String secret) {
        this(webhook, secret, defaultExecutor);
    }

    public DingTalkClientImpl(String webhook,@Nullable String secret, AsyncTaskExecutor executor) {
        this.webhook = webhook;
        this.secret = secret;
        this.executor = executor;
    }

    @Override
    public void send(Message message) {
        send(message, webhook);
    }

    @Override
    public void send(Message message, String webhook) {
        send(message, webhook, secret);
    }

    @Override
    public void send(Message message, String webhook,@Nullable String secret) {
        if (!StringUtils.hasText(webhook)) {
            log.warn("未配置钉钉webhook");
            return;
        }
        executor.execute(() -> sendSync(message, webhook, secret));
    }

    public boolean sendSync(Message message, String webhook,@Nullable String secret) {
        var params = "";
        try {
            var body = new HashMap<String, Object>(4);
            body.put("msgtype", message.type());
            body.put(message.type(), message);
            if (message instanceof At at) {
                var atNode = new HashMap<String, Object>(2);
                atNode.put("atMobiles", at.atMobiles());
                atNode.put("isAtAll", at.isAtAll());
                body.put("at", atNode);
            }
            params = JsonbUtils.toJson(body);
            String url;
            if (StringUtils.hasText(secret)) {
                long timestamp = System.currentTimeMillis();
                url = webhook +"&timestamp=" + timestamp + "&sign=" + URLEncoder.encode(Base64.getEncoder().encodeToString(HMACUtils.sha256(timestamp+"\n"+secret, secret)), StandardCharsets.UTF_8);
            }else {
                url = webhook;
            }
            var resStr = RestClientUtils.post().uri(url).contentType(MediaType.APPLICATION_JSON)
                    .body(params).retrieve().body(String.class);
            var res = responseType.fromJson(resStr);
            if (res.errcode() != 0) {
                log.error("钉钉机器人发送信息失败，参数：{}，webhook：{}，响应信息：{}", params, webhook, resStr);
                return false;
            }
        } catch (Exception e) {
            log.error("钉钉机器人发送信息失败，参数：{}，webhook：{}，异常信息：", params, webhook, e);
            return false;
        }
        return true;
    }

    /**
     * 钉钉webhook响应
     */
    @Json
    record Response(int errcode, String errmsg) {
    }

}
