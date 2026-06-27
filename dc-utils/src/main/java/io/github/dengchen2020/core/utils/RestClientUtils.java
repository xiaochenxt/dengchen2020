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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.http.client.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
                .messageConverters(RestClientUtils::moveXmlConverterToLast);
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

    static volatile RestClient noSSL;

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
     * @param httpClient {@link HttpClient}
     * @param readTimeout 读取超时时间，默认10秒
     * @return {@link RestClient}
     */
    private static RestClient create(HttpClient httpClient, Duration readTimeout) {
        RestClient.Builder builder = builder(createFactory(httpClient, readTimeout, true));
        return builder.build();
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
            } else if (httpMessageConverter instanceof AllEncompassingFormHttpMessageConverter allEncompassingFormHttpMessageConverter) {
                var list = new ArrayList<>(allEncompassingFormHttpMessageConverter.getPartConverters());
                moveXmlConverterToLast(list);
                allEncompassingFormHttpMessageConverter.setPartConverters(list);
            } else if (httpMessageConverter instanceof MappingJackson2XmlHttpMessageConverter || httpMessageConverter instanceof Jaxb2RootElementHttpMessageConverter) {
                iterator.remove();
                converters.addLast(httpMessageConverter); // todo spring7.0开始，重新进行了排序，json在xml之前，因此从springboot4.0开始，不再需要排序
                break;
            }
        }
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

    abstract static class AbstractBufferingClientHttpRequest extends AbstractClientHttpRequest {
        private final FastByteArrayOutputStream bufferedOutput = new FastByteArrayOutputStream(1024);
        @Override
        protected OutputStream getBodyInternal(HttpHeaders headers) throws IOException {
            return this.bufferedOutput;
        }
        @Override
        protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
            byte[] bytes = this.bufferedOutput.toByteArrayUnsafe();
            if (headers.getContentLength() < 0) {
                headers.setContentLength(bytes.length);
            }
            ClientHttpResponse result = executeInternal(headers, bytes);
            this.bufferedOutput.reset();
            return result;
        }
        protected abstract ClientHttpResponse executeInternal(HttpHeaders headers, byte[] bufferedOutput)
                throws IOException;
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
                if (this.request instanceof StreamingHttpOutputMessage streamingHttpOutputMessage) {
                    streamingHttpOutputMessage.setBody(new StreamingHttpOutputMessage.Body() {
                        @Override
                        public void writeTo(OutputStream outputStream) throws IOException {
                            StreamUtils.copy(bufferedOutput, outputStream);
                        }
                        @Override
                        public boolean repeatable() {
                            return true;
                        }
                    });
                }
                else {
                    StreamUtils.copy(bufferedOutput, this.request.getBody());
                }
            }
            return this.request.execute();
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
