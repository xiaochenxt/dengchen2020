package io.github.dengchen2020.core.utils;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.client.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * RestClient工具类
 * @author xiaochen
 * @since 2024/5/14
 */
@NullMarked
public abstract class RestClientUtils {

    /**
     * 默认每个路由最大连接数，200
     */
    static final int DEFAULT_MAX_CONN_PER_ROUTE = Integer.parseInt(System.getProperty("restClient.maxConnPerRoute", "200"));

    /**
     * 默认读取超时时间，10秒钟
     */
    static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(Integer.parseInt(System.getProperty("restClient.readTimeout", "10")));

    /**
     * 默认连接超时时间，30秒钟
     */
    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(Integer.parseInt(System.getProperty("restClient.connectTimeout", "30")));

    static final Set<HttpMethod> BUFFER_CONTENT_METHODS = Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

    static final RestClient client = create(DEFAULT_MAX_CONN_PER_ROUTE, DEFAULT_READ_TIMEOUT);

    public static RestClient.RequestBodyUriSpec method(HttpMethod method) {
        return client.method(method);
    }

    public static RestClient.RequestHeadersUriSpec<?> get() {
        return client.get();
    }

    public static RestClient.RequestBodyUriSpec post() {
        return client.post();
    }

    public static RestClient.RequestBodyUriSpec put() {
        return client.put();
    }

    public static RestClient.RequestHeadersUriSpec<?> delete() {
        return client.delete();
    }

    public static RestClient.RequestHeadersUriSpec<?> options() {
        return client.options();
    }

    public static RestClient.RequestBodyUriSpec patch() {
        return client.patch();
    }

    public static RestClient.RequestHeadersUriSpec<?> head() {
        return client.head();
    }

    public static RestClient.Builder mutate() {
        return client.mutate();
    }

    public static ClientHttpRequestFactory createFactory(boolean bufferContent) {
        return createFactory(createHttpClient(DEFAULT_MAX_CONN_PER_ROUTE), DEFAULT_READ_TIMEOUT, bufferContent);
    }

    public static RestClient.Builder builder(ClientHttpRequestFactory factory) {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE)
                .requestFactory(factory)
                .configureMessageConverters(messageConverters -> {
                    messageConverters.registerDefaults();
                    messageConverters.withStringConverter(new StringHttpMessageConverter(StandardCharsets.UTF_8));
                });
    }

    /**
     * 创建RestClient实例
     *
     * @param maxConnPerRoute 每个路由的最大连接数
     * @param readTimeout 读取超时时间
     * @return {@link RestClient}
     */
    public static RestClient create(int maxConnPerRoute, Duration readTimeout) {
        return create(createHttpClient(maxConnPerRoute), readTimeout);
    }

    static volatile @Nullable RestClient noSSL;

    /**
     * 不校验SSL证书，非测试环境或内网环境禁止使用
     * @return {@link RestClient}
     */
    public static RestClient noSSL() {
        RestClient client;
        if ((client = noSSL) == null) {
            synchronized (RestClientUtils.class) {
                if ((client = noSSL) == null) {
                    try {
                        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)
                                .setMaxConnPerRoute(DEFAULT_MAX_CONN_PER_ROUTE)
                                .setTlsSocketStrategy(new DefaultClientTlsStrategy(SSLContexts.custom().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build()))
                                .build();
                        noSSL = client = create(HttpClientBuilder.create().setConnectionManager(connectionManager).build(), DEFAULT_READ_TIMEOUT);
                    } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                        throw new RuntimeException("创建无需SSL证书校验的RestClient失败", e);
                    }
                }
            }
        }
        return client;
    }

    private static HttpClient createHttpClient(int maxConnPerRoute) {
        HttpClientConnectionManager manager = PoolingHttpClientConnectionManagerBuilder.create()
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)
                .setMaxConnPerRoute(maxConnPerRoute <= 0 ? DEFAULT_MAX_CONN_PER_ROUTE : maxConnPerRoute)
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                        .build())
                .build();
        return HttpClientBuilder.create().setConnectionManager(manager).build();
    }

    private static ClientHttpRequestFactory createFactory(HttpClient httpClient, Duration readTimeout, boolean bufferContent) {
        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(readTimeout);
        return bufferContent ? new OptimizedBufferingClientHttpRequestFactory(factory) : factory;
    }

    /**
     * 创建RestClient实例
     *
     * @param httpClient {@link HttpClient}
     * @param readTimeout 读取超时时间，默认10秒
     * @return {@link RestClient}
     */
    private static RestClient create(HttpClient httpClient, Duration readTimeout) {
        RestClient.Builder builder = builder(createFactory(httpClient, readTimeout, true));
        return builder.build();
    }

    /**
     * 优化后的缓冲区请求工厂，主要是为了解决部分第三方接口要求必须带有content-length请求头的问题
     */
    static final class OptimizedBufferingClientHttpRequestFactory extends AbstractClientHttpRequestFactoryWrapper {
        public OptimizedBufferingClientHttpRequestFactory(ClientHttpRequestFactory requestFactory) {
            super(requestFactory);
        }
        @Override
        protected ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod, ClientHttpRequestFactory requestFactory) throws IOException {
            ClientHttpRequest request = requestFactory.createRequest(uri, httpMethod);
            if (BUFFER_CONTENT_METHODS.contains(httpMethod)) return new OptimizedBufferingClientHttpRequest(request);
            return request;
        }
    }

    static final class OptimizedBufferingClientHttpRequest extends AbstractBufferingClientHttpRequest {
        private final ClientHttpRequest request;
        public OptimizedBufferingClientHttpRequest(ClientHttpRequest request) {
            this.request = request;
        }
        @Override
        protected ClientHttpResponse executeInternal(HttpHeaders headers, byte[] bufferedOutput) throws IOException {
            this.request.getHeaders().putAll(headers);
            if (bufferedOutput.length > 0) {
                long contentLength = request.getHeaders().getContentLength();
                if (contentLength > -1 && contentLength != bufferedOutput.length) {
                    request.getHeaders().setContentLength(bufferedOutput.length);
                }
                if (request instanceof StreamingHttpOutputMessage streamingOutputMessage) {
                    streamingOutputMessage.setBody(bufferedOutput);
                }
                else {
                    StreamUtils.copy(bufferedOutput, request.getBody());
                }
            }
            return request.execute();
        }
        @Override
        public HttpMethod getMethod() {
            return request.getMethod();
        }
        @Override
        public URI getURI() {
            return request.getURI();
        }
    }

}
