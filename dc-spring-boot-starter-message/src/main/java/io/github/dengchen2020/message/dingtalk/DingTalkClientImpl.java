package io.github.dengchen2020.message.dingtalk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.dengchen2020.core.utils.JsonUtils;
import io.github.dengchen2020.core.utils.RestClientUtils;
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

/**
 * 钉钉webhook推送
 *
 * @author xiaochen
 * @since 2023/3/11
 */
@NullMarked
public class DingTalkClientImpl implements DingTalkClient {

    private static final Logger log = LoggerFactory.getLogger(DingTalkClientImpl.class);

    private final AsyncTaskExecutor executor;

    private static final AsyncTaskExecutor defaultExecutor = new VirtualThreadTaskExecutor("dingTalk-");

    private final String webhook;

    @Nullable
    private final String secret;

    public DingTalkClientImpl(String webhook) {
        this.webhook = webhook;
        this.secret = null;
        this.executor = defaultExecutor;
    }

    public DingTalkClientImpl(String webhook, String secret) {
        this.webhook = webhook;
        this.secret = secret;
        this.executor = defaultExecutor;
    }

    public DingTalkClientImpl(String webhook, String secret, AsyncTaskExecutor executor) {
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
        ObjectNode params = JsonUtils.createObjectNode();
        try {
            params.put("msgtype", message.type());
            params.putPOJO(message.type(), message);
            if (message instanceof At at) {
                ObjectNode atNode = JsonUtils.createObjectNode();
                atNode.putPOJO("atMobiles", at.atMobiles());
                atNode.put("isAtAll", at.isAtAll());
                params.set("at", atNode);
            }
            String url;
            if (StringUtils.hasText(secret)) {
                long timestamp = System.currentTimeMillis();
                url = webhook +"&timestamp=" + timestamp + "&sign=" + URLEncoder.encode(Base64.getEncoder().encodeToString(HMACUtils.sha256(timestamp+"\n"+secret, secret)), StandardCharsets.UTF_8);
            }else {
                url = webhook;
            }
            JsonNode res = RestClientUtils.post().uri(url).contentType(MediaType.APPLICATION_JSON).body(params).retrieve().body(JsonNode.class);
            if (res.path("errcode").asInt() != 0) {
                log.error("钉钉机器人发送信息失败，参数：{}，webhook：{}，响应信息：{}", params, webhook, res);
                return false;
            }
        } catch (Exception e) {
            log.error("钉钉机器人发送信息失败，参数：{}，webhook：{}，异常信息：", params, webhook, e);
            return false;
        }
        return true;
    }

}
