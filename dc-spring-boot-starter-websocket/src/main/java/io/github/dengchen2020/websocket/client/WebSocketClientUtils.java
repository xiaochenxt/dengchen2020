package io.github.dengchen2020.websocket.client;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;
import org.apache.tomcat.websocket.WsWebSocketContainer;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.ee11.websocket.jakarta.client.JakartaWebSocketClientContainerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ClassUtils;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.channels.AsynchronousChannelGroup;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * websocket客户端工具类
 * <p>使用websocket综合推荐 jetty</p>
 * <p>内存占用对比（数字为占用比例，tomcat非常吃内存）：tomcat（10） > jetty（2.5） > undertow（1）</p>
 * @author xiaochen
 * @since 2025/4/24
 */
public class WebSocketClientUtils {

    private static final AsyncTaskExecutor defaultExecutor = new VirtualThreadTaskExecutor("ws-client-");

    private static final WebSocketContainer webSocketContainer;

    private static final boolean jakartaWebSocketClientContainerProviderPresent;
    private static final boolean wsWebSocketContainerPresent;

    static {
        ClassLoader classLoader = WebSocketClientUtils.class.getClassLoader();
        jakartaWebSocketClientContainerProviderPresent = ClassUtils.isPresent("org.eclipse.jetty.ee11.websocket.jakarta.client.JakartaWebSocketClientContainerProvider", classLoader);
        wsWebSocketContainerPresent = ClassUtils.isPresent("org.apache.tomcat.websocket.WsWebSocketContainer", classLoader);
        // jetty默认配置会导致websocket连接变多后线程数暴涨，tomcat和undertow不会
        if (jakartaWebSocketClientContainerProviderPresent) {
            HttpClient httpClient = new HttpClient();
            httpClient.setExecutor(defaultExecutor);
            webSocketContainer = JakartaWebSocketClientContainerProvider.getContainer(httpClient);
        } else // tomcat默认配置会导致程序停止时大量websocket连接关闭的时刻（也许是一瞬间）线程数会暴涨
            if (wsWebSocketContainerPresent){
                WsWebSocketContainer container = new WsWebSocketContainer();
                webSocketContainer = container;
                try {
                    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(WsWebSocketContainer.class, MethodHandles.lookup());
                    final VarHandle asynchronousChannelGroupMethod = lookup.findVarHandle(
                            WsWebSocketContainer.class,
                            "asynchronousChannelGroup",
                            AsynchronousChannelGroup.class
                    );
                    asynchronousChannelGroupMethod.set(container, AsynchronousChannelGroup.withThreadPool(Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("ws-client").factory())));
                } catch (NoSuchFieldException e) {
                    throw new IllegalStateException("未找到字段：org.apache.tomcat.websocket.WsWebSocketContainer.asynchronousChannelGroup", e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("无法访问字段：org.apache.tomcat.websocket.WsWebSocketContainer.asynchronousChannelGroup", e);
                } catch (IOException e) {
                    throw new RuntimeException("设置asynchronousChannelGroup为虚拟线程失败", e);
                }
                Runtime.getRuntime().addShutdownHook(new Thread(container::destroy, "ws-client-shutdown"));
            } else {
            webSocketContainer = ContainerProvider.getWebSocketContainer();
        }
        if (webSocketContainer == null) throw new IllegalStateException("未找到可用的websocket客户端容器实现");
        webSocketContainer.setDefaultMaxTextMessageBufferSize(8192);
        webSocketContainer.setDefaultMaxBinaryMessageBufferSize(8192);
        webSocketContainer.setDefaultMaxSessionIdleTimeout(0);
        webSocketContainer.setAsyncSendTimeout(10000);
    }

    /**
     * 创建一个websocket客户端
     *
     * @return {@link StandardWebSocketClient}
     */
    public static StandardWebSocketClient createClient() {
        return createClient(null, null);
    }

    /**
     * 创建一个websocket客户端
     *
     * @return {@link StandardWebSocketClient}
     */
    public static StandardWebSocketClient createClient(Map<String, Object> userProperties, SSLContext sslContext) {
        StandardWebSocketClient client = new StandardWebSocketClient(webSocketContainer);
        client.setTaskExecutor(defaultExecutor);
        if (userProperties != null) client.setUserProperties(userProperties);
        if (sslContext != null) client.setSslContext(sslContext);
        return client;
    }

    /**
     * 创建一个websocket客户端会话，会话用完后需由调用方关闭
     *
     * @param url                  websocket连接地址
     * @param webSocketHttpHeaders websocket请求头
     * @param webSocketHandler     websocket处理器
     * @return {@link WebSocketSession}
     */
    public static WebSocketSession createSession(String url, WebSocketHttpHeaders webSocketHttpHeaders, WebSocketHandler webSocketHandler) {
        WebSocketSession session = createClient().execute(webSocketHandler, webSocketHttpHeaders, URI.create(url)).join();
        return wrap(session);
    }

    /**
     * 创建一个websocket客户端会话，会话用完后需由调用方关闭
     *
     * @param url              websocket连接地址
     * @param webSocketHandler websocket处理器
     * @return {@link WebSocketSession}
     */
    public static WebSocketSession createSession(String url, WebSocketHandler webSocketHandler) {
        return createSession(url, null, webSocketHandler);
    }

    /**
     * 使用 {@link ConcurrentWebSocketSessionDecorator} 包装
     *
     * @param session {@link WebSocketSession}
     * @return {@link ConcurrentWebSocketSessionDecorator}
     */
    public static WebSocketSession wrap(WebSocketSession session) {
        return new ConcurrentWebSocketSessionDecorator(session, 1000 * 10, 1024 * 32);
    }

    /**
     * 创建一个掉线自动重连的websocket客户端会话，会话用完后需由调用方关闭
     *
     * @param url              websocket连接地址
     * @param webSocketHandler websocket处理器
     * @param maxReconnectAttempts 最多尝试重连次数
     * @return {@link WebSocketSession}
     */
    public static WebSocketSession createSession(String url, WebSocketHandler webSocketHandler, int maxReconnectAttempts) {
        return createSession(url, null, webSocketHandler, maxReconnectAttempts);
    }

    /**
     * 创建一个掉线自动重连的websocket客户端会话，会话用完后需由调用方关闭
     *
     * @param url              websocket连接地址
     * @param webSocketHttpHeaders websocket请求头
     * @param webSocketHandler websocket处理器
     * @param maxReconnectAttempts 最多尝试重连次数
     * @return {@link WebSocketSession}
     */
    public static WebSocketSession createSession(String url, WebSocketHttpHeaders webSocketHttpHeaders, WebSocketHandler webSocketHandler, int maxReconnectAttempts) {
        return new WebSocketSessionReconnectSupport(url, webSocketHttpHeaders, webSocketHandler, maxReconnectAttempts);
    }

    public static class WebSocketSessionReconnectSupport implements WebSocketSession {

        private static final Logger log = LoggerFactory.getLogger(WebSocketClientUtils.class);

        private volatile WebSocketSession session;

        private final AtomicBoolean reconnecting = new AtomicBoolean();

        private final int maxReconnectAttempts;

        public WebSocketSessionReconnectSupport(String url, WebSocketHttpHeaders webSocketHttpHeaders, WebSocketHandler webSocketHandler, Function<CloseStatus, Boolean> isReconnect, int maxReconnectAttempts) {
            this.maxReconnectAttempts = Math.max(maxReconnectAttempts, 1);
            WebSocketHandler wrap = new WebSocketHandlerDecorator(webSocketHandler) {
                @Override
                public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
                    super.afterConnectionClosed(webSocketSession, closeStatus);
                    if (!closeStatus.equalsCode(CloseStatus.NORMAL) && isReconnect.apply(closeStatus) && reconnecting.compareAndSet(false, true)) {
                        Thread.ofVirtual().name("websocket-client-reconnect").start(() -> {
                            try {
                                log.info("websocket连接断开，原因：{}，客户端开始重连，url：{}，headers：{}", closeStatus, url, webSocketHttpHeaders);
                                reconnect(url, webSocketHttpHeaders, this, closeStatus);
                            } finally {
                                reconnecting.set(false);
                            }
                        });
                    }
                }
            };
            setSession(createSession(url, wrap));
        }

        public WebSocketSessionReconnectSupport(String url, WebSocketHttpHeaders webSocketHttpHeaders, WebSocketHandler webSocketHandler, int maxReconnectAttempts) {
            this(url, webSocketHttpHeaders, webSocketHandler, (closeStatus -> true), maxReconnectAttempts);
        }

        protected long nextReconnectTime(CloseStatus closeStatus, int attempt) {
            long ms;
            if (closeStatus.equalsCode(CloseStatus.GOING_AWAY)) {
                ms = 1000 * 3;
            } else if (closeStatus.equalsCode(CloseStatus.SERVICE_RESTARTED)) {
                ms = 1000 * 5;
            } else if (closeStatus.equalsCode(CloseStatus.SERVICE_OVERLOAD)) {
                ms = 1500;
            } else {
                return 3000L * (attempt - 1);
            }
            return Math.min(ms * attempt, 1000L * 30);
        }

        private void reconnect(String url, WebSocketHttpHeaders webSocketHttpHeaders, WebSocketHandler webSocketHandler, CloseStatus closeStatus) {
            for (int attempt = 1; attempt <= maxReconnectAttempts; attempt++) {
                try {
                    Thread.sleep(nextReconnectTime(closeStatus, attempt));
                    WebSocketSession newSession = createSession(url, webSocketHttpHeaders, webSocketHandler);
                    if (newSession.isOpen()) {
                        setSession(newSession);
                        log.info("websocket客户端重连成功，url：{}，headers：{}", url, webSocketHttpHeaders);
                        return;
                    }
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (CompletionException | IllegalStateException e) {
                    if (attempt >= maxReconnectAttempts) {
                        log.error("websocket客户端第{}次重连失败，url：{}，headers：{}，不再重连", attempt, url, webSocketHttpHeaders);
                        return;
                    }
                    if (log.isDebugEnabled()) log.debug("websocket客户端第{}次重连失败，将在{}毫秒后自动重连", attempt, nextReconnectTime(closeStatus, attempt + 1));
                }
            }
        }

        private void setSession(WebSocketSession session) {
            this.session = session;
        }

        @Override
        public String getId() {
            return session.getId();
        }

        @Override
        public URI getUri() {
            return session.getUri();
        }

        @Override
        public HttpHeaders getHandshakeHeaders() {
            return session.getHandshakeHeaders();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return session.getAttributes();
        }

        @Override
        public Principal getPrincipal() {
            return session.getPrincipal();
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return session.getLocalAddress();
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return session.getRemoteAddress();
        }

        @Override
        public String getAcceptedProtocol() {
            return session.getAcceptedProtocol();
        }

        @Override
        public void setTextMessageSizeLimit(int messageSizeLimit) {
            if (!isOpen()) return;
            session.setTextMessageSizeLimit(messageSizeLimit);
        }

        @Override
        public int getTextMessageSizeLimit() {
            return session.getTextMessageSizeLimit();
        }

        @Override
        public void setBinaryMessageSizeLimit(int messageSizeLimit) {
            if (!isOpen()) return;
            session.setBinaryMessageSizeLimit(messageSizeLimit);
        }

        @Override
        public int getBinaryMessageSizeLimit() {
            return session.getBinaryMessageSizeLimit();
        }

        @Override
        public List<WebSocketExtension> getExtensions() {
            return session.getExtensions();
        }

        @Override
        public void sendMessage(WebSocketMessage<?> message) throws IOException {
            if (!isOpen()) return;
            session.sendMessage(message);
        }

        @Override
        public boolean isOpen() {
            return session.isOpen();
        }

        @Override
        public void close() throws IOException {
            session.close();
        }

        @Override
        public void close(CloseStatus status) throws IOException {
            session.close(status);
        }
    }

}
