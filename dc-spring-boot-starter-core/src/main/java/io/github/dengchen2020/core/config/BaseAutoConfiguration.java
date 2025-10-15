package io.github.dengchen2020.core.config;

import io.github.dengchen2020.core.utils.RestClientUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;

/**
 * 基础自动配置
 *
 * @author xiaochen
 * @since 2024/5/31
 */
@PropertySource("classpath:application-core.properties")
@Configuration(proxyBeanMethods = false)
public class BaseAutoConfiguration implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(BaseAutoConfiguration.class);

    private final Environment environment;

    public BaseAutoConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() {
        if (!Charset.defaultCharset().equals(StandardCharsets.UTF_8)) log.warn("JAVA默认字符集：{}，非UTF-8可能导致字符乱码", Charset.defaultCharset());
        cglibDebug();
        if (!Threading.VIRTUAL.isActive(environment)) throw new IllegalStateException("请配置spring.threads.virtual.enabled=true");
    }

    private void cglibDebug() {
        Set<String> activeProfiles = Set.of(environment.getActiveProfiles());
        if (!activeProfiles.contains("dev")) return;
        boolean cglibDebug = environment.getProperty("dc.debug.cglib.enabled", Boolean.class, true);
        if (!cglibDebug) return;
        String rootDir = System.getProperty("user.dir");
        String targetDir = rootDir + File.separator + "target";
        if (!Files.exists(Path.of(targetDir))) return;
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, targetDir + File.separator + "cglib");
    }

    @ConditionalOnClass(name = "org.apache.hc.client5.http.classic.HttpClient")
    @Configuration(proxyBeanMethods = false)
    static class RestClientAutoConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory() {
            HttpClientConnectionManager manager = PoolingHttpClientConnectionManagerBuilder.create()
                    .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.LAX)
                    .setMaxConnPerRoute(200)
                    .build();
            HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(manager).build();
            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setReadTimeout(Duration.ofSeconds(10));
            factory.setConnectTimeout(Duration.ofSeconds(30));
            return factory;
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        @ConditionalOnMissingBean
        public RestClient.Builder httpClientBuilder(RestClientBuilderConfigurer restClientBuilderConfigurer, HttpComponentsClientHttpRequestFactory factory) {
            RestClient.Builder builder = restClientBuilderConfigurer.configure(RestClientUtils.builder(factory));
            return builder;
        }

        @Bean
        @ConditionalOnMissingBean
        public RestClient restClient(HttpComponentsClientHttpRequestFactory factory) {
            return RestClientUtils.builder(factory).build();
        }
    }

    @Bean
    static BeanFactoryPostProcessor taskExecutorAliasBeanFactoryPostProcessor() {
        return (beanFactory) -> beanFactory.registerAlias(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME, "taskExecutor");
    }

}
