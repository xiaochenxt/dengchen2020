package io.github.dengchen2020.message.feishu;

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

import java.util.Base64;
import java.util.HashMap;

/**
 * 飞书webhook推送
 *
 * @author xiaochen
 * @since 2023/7/4
 */
@NullMarked
public class FeiShuClientImpl implements FeiShuClient {

    private static final Logger log = LoggerFactory.getLogger(FeiShuClientImpl.class);

    private static final JsonType<Response> responseType = JsonbUtils.type(Response.class);

    private final AsyncTaskExecutor executor;

    private static final AsyncTaskExecutor defaultExecutor = new VirtualThreadTaskExecutor("feiShu-");

    private final String webhook;

    @Nullable
    private final String secret;

    public FeiShuClientImpl(String webhook) {
        this(webhook, null, defaultExecutor);
    }

    public FeiShuClientImpl(String webhook,@Nullable String secret) {
        this(webhook, secret, defaultExecutor);
    }

    public FeiShuClientImpl(String webhook,@Nullable String secret, AsyncTaskExecutor executor) {
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
            log.warn("未配置飞书webhook");
            return;
        }
        executor.execute(() -> sendSync(message, webhook, secret));
    }

    public boolean sendSync(Message message, String webhook,@Nullable String secret) {
        var params = "";
        try {
            var body = new HashMap<String, Object>(4);
            body.put("msg_type", message.type());
            body.put("content", message);
            if (StringUtils.hasText(secret)) {
                var timestamp = String.valueOf(System.currentTimeMillis() / 1000);
                body.put("timestamp", timestamp);
                body.put("sign", Base64.getEncoder().encodeToString(HMACUtils.sha256("", timestamp + "\n" + secret)));
            }
            params = JsonbUtils.toJson(body);
            var resStr = RestClientUtils.post().uri(webhook)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(params).retrieve().body(String.class);
            var res = responseType.fromJson(resStr);
            if (res.code() != 0) {
                log.error("飞书机器人发送信息失败，参数：{}，webhook：{}，响应信息：{}", params, webhook, resStr);
                return false;
            }
        } catch (Exception e) {
            log.error("飞书机器人发送信息失败，参数：{}，webhook：{}，异常信息：", params, webhook, e);
            return false;
        }
        return true;
    }

    /**
     * 飞书webhook响应
     */
    @Json
    record Response(int code, String msg) {
    }
}
