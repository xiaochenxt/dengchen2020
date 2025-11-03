package io.github.dengchen2020.core.utils;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.ssl.SSLContexts;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;

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
    static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(Integer.parseInt(System.getProperty("restClient.connectTimeout", "30")));;

    static final RestClient client = create(DEFAULT_MAX_CONN_PER_ROUTE);

    static final RestClient clientNoBuffering = createNoBuffering(DEFAULT_MAX_CONN_PER_ROUTE);

    /**
     * 通过 POST 或 PUT 发送大量数据时，推荐使用这个，以免内存不足
     *
     * @return {@link RestClient}
     */
    public static RestClient noBuffering() {
        return clientNoBuffering;
    }

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

    public static RestClient.Builder builder(ClientHttpRequestFactory factory) {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE)
                .requestFactory(factory)
                .messageConverters(RestClientUtils::moveXmlConverterToLast);
    }

    /**
     * 3.4.4版本RestClient新增了很多http消息转换器，其中包含xml，而xml被排在了json前面，请求优先以xml数据传输导致只接收json数据的接口报错，而json更为流行，应比xml优先级高才对
     *
     * @param converters
     */
    private static void moveXmlConverterToLast(List<HttpMessageConverter<?>> converters) {
        for (Iterator<HttpMessageConverter<?>> iterator = converters.iterator(); iterator.hasNext(); ) {
            HttpMessageConverter<?> httpMessageConverter = iterator.next();
            if (httpMessageConverter instanceof StringHttpMessageConverter stringHttpMessageConverter) {
                stringHttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
                continue;
            }
            if (httpMessageConverter instanceof MappingJackson2XmlHttpMessageConverter) {
                iterator.remove();
                converters.addLast(httpMessageConverter);
                break;
            }
        }
    }


    static final RestClient noSSL = createNoSSL();

    /**
     * 不校验SSL证书，非测试环境或内网环境禁止使用
     * @return {@link RestClient}
     */
    public static RestClient noSSL() {
        return noSSL;
    }

    /**
     * 创建一个HttpClient
     *
     * @param maxConnPerRoute 每个路由的最大连接数
     * @return {@link RestClient}
     */
    private static HttpClientBuilder httpClientBuilder(int maxConnPerRoute) {
        HttpClientConnectionManager manager = PoolingHttpClientConnectionManagerBuilder.create()
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)
                .setMaxConnPerRoute(maxConnPerRoute <= 0 ? DEFAULT_MAX_CONN_PER_ROUTE : maxConnPerRoute)
                .build();
        return HttpClientBuilder.create().setConnectionManager(manager);
    }

    private static HttpComponentsClientHttpRequestFactory createFactory(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(DEFAULT_READ_TIMEOUT);
        factory.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        return factory;
    }

    /**
     * 使用 {@link BufferingClientHttpRequestFactory} 包装以设置contentLength，但会增加内存占用
     *
     * @param maxConnPerRoute 每个路由的最大连接数
     * @return {@link RestClient}
     */
    public static RestClient create(int maxConnPerRoute) {
        return create(httpClientBuilder(maxConnPerRoute).build(), false);
    }

    /**
     * 不会设置contentLength，可能会导致请求部分第三方接口报错，因为要求需要携带contentLength
     *
     * @param maxConnPerRoute 每个路由的最大连接数
     * @return {@link RestClient}
     */
    public static RestClient createNoBuffering(int maxConnPerRoute) {
        return create(httpClientBuilder(maxConnPerRoute).build(), true);
    }

    /**
     * 使用 {@link BufferingClientHttpRequestFactory} 包装以设置contentLength，但会增加内存占用
     *
     * @param httpClient {@link HttpClient}
     * @return {@link RestClient}
     */
    private static RestClient create(HttpClient httpClient, boolean noBuffering) {
        ClientHttpRequestFactory factory = createFactory(httpClient);
        RestClient.Builder builder = builder(noBuffering ? factory : new BufferingClientHttpRequestFactory(factory));
        return builder.build();
    }

    private static RestClient createNoSSL() {
        try {
            HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create().setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)
                    .setMaxConnPerRoute(DEFAULT_MAX_CONN_PER_ROUTE)
                    .setTlsSocketStrategy(new DefaultClientTlsStrategy(SSLContexts.custom().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build()))
                    .build();
            return create(HttpClientBuilder.create().setConnectionManager(connectionManager).build(), true);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

}
