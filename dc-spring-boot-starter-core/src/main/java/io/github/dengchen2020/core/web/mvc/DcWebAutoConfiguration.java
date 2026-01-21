package io.github.dengchen2020.core.web.mvc;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import io.github.dengchen2020.core.filter.DcShallowEtagHeaderFilter;
import io.github.dengchen2020.core.properties.DcCorsProperties;
import io.github.dengchen2020.core.properties.DcETagProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.CachingResourceTransformer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.ResourceHandlerUtils;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING_ARRAY;

/**
 * Web自动配置
 *
 * @author xiaochen
 * @since 2025/6/23
 */
@NullMarked
@EnableConfigurationProperties(DcCorsProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration(proxyBeanMethods = false)
public final class DcWebAutoConfiguration implements WebMvcConfigurer, ApplicationContextAware {

    private final WebProperties.Resources resources;
    @Nullable
    private ApplicationContext applicationContext;

    public DcWebAutoConfiguration(WebProperties webProperties) {
        this.resources = webProperties.getResources();
        if (resources.isAddMappings()) throw new IllegalStateException("请配置spring.web.resources.add-mappings=false");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @ConditionalOnProperty(value = "dc.cors.enabled", matchIfMissing = true, havingValue = "true")
    @ConditionalOnMissingBean(value = CorsFilter.class, parameterizedContainer = FilterRegistrationBean.class)
    @Bean
    FilterRegistrationBean<CorsFilter> corsFilterFilterRegistrationBean(DcCorsProperties builder) {
        CorsConfiguration config = new CorsConfiguration();
        if (StringUtils.hasText(builder.getAllowedOriginPatterns())) {
            config.addAllowedOriginPattern(builder.getAllowedOriginPatterns());
        } else if (!CollectionUtils.isEmpty(builder.getAllowedOrigins())) {
            config.setAllowedOrigins(builder.getAllowedOrigins());
        } else {
            config.addAllowedOriginPattern("*");
        }
        if (!CollectionUtils.isEmpty(builder.getAllowedHeaders())) {
            config.setAllowedHeaders(builder.getAllowedHeaders());
        } else {
            config.addAllowedHeader("*");
        }
        if (!CollectionUtils.isEmpty(builder.getExposedHeaders())) {
            config.setExposedHeaders(builder.getExposedHeaders());
        } else {
            config.addExposedHeader("*");
        }
        if (!CollectionUtils.isEmpty(builder.getAllowedMethods())) {
            config.setAllowedMethods(builder.getAllowedMethods());
        } else {
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "QUERY"));
        }
        if (builder.getAllowCredentials() != null) {
            config.setAllowCredentials(builder.getAllowCredentials());
        } else {
            config.setAllowCredentials(true);
        }
        if (builder.getAllowPrivateNetwork() != null) {
            config.setAllowPrivateNetwork(builder.getAllowPrivateNetwork());
        } else {
            config.setAllowPrivateNetwork(false);
        }
        if (builder.getMaxAge() != null) {
            config.setMaxAge(builder.getMaxAge());
        } else {
            config.setMaxAge(Duration.ofDays(30));
        }
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new CorsFilter(source));
        filterRegistrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return filterRegistrationBean;
    }

    @EnableConfigurationProperties(DcETagProperties.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(value = "dc.etag.enabled", havingValue = "true")
    @ConditionalOnMissingBean(value = ShallowEtagHeaderFilter.class, parameterizedContainer = FilterRegistrationBean.class)
    @Configuration(proxyBeanMethods = false)
    static final class EtagAutoConfiguration {

        private final DcETagProperties properties;

        EtagAutoConfiguration(DcETagProperties properties) {
            this.properties = properties;
        }

        @Bean
        FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilterFilterRegistrationBean() {
            ShallowEtagHeaderFilter filter = new DcShallowEtagHeaderFilter(properties.getIgnorePath().toArray(EMPTY_STRING_ARRAY));
            if (properties.isWriteWeakETag()) filter.setWriteWeakETag(properties.isWriteWeakETag());
            FilterRegistrationBean<ShallowEtagHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
            filterRegistrationBean.setFilter(filter);
            return filterRegistrationBean;
        }

        @ConditionalOnMissingBean
        @Bean
        EtagOptimizeResponseBodyAdvice etagOptimizeResponseBodyAdvice() {
            return new EtagOptimizeResponseBodyAdvice(properties.getMaxLength().toBytes());
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (applicationContext == null) throw new IllegalStateException("applicationContext is null");
        Set<String> locations = new HashSet<>();
        for (String staticLocation : resources.getStaticLocations()) {
            if (staticLocation.contains("classpath")) {
                var resource = applicationContext.getResource(ResourceHandlerUtils.initLocationPath(staticLocation));
                if (resource.exists()) locations.add(staticLocation);
            } else {
                locations.add(staticLocation);
            }
        }
        if (locations.isEmpty()) return;
        var environment = applicationContext.getEnvironment();
        var pathPatters = environment.getProperty("spring.mvc.static-path-pattern","/**");
        var maximumSize = environment.getProperty("dc.static.resource.cache.maximum-size", Integer.class, 1000);
        var maxContentLength = environment.getProperty("dc.static.resource.cache.max-content-length", DataSize.class, DataSize.ofKilobytes(256)).toBytes();
        var ttl = environment.getProperty("dc.static.resource.cache.ttl", Duration.class, Duration.ofSeconds(5));
        var cache = new CaffeineCache("spring-resource-chain-cache", Caffeine.newBuilder()
                .maximumSize(Math.max(100, maximumSize))
                .expireAfterWrite(ttl)
                .scheduler(Scheduler.forScheduledExecutorService(Executors.newScheduledThreadPool(1, Thread.ofVirtual().name("resource-cache-clear",0).factory())))
                .softValues()
                .build());
        var cachePeriod = resources.getCache().getPeriod();
        var cacheControl = resources.getCache().getCachecontrol().toHttpCacheControl();
        var registration = registry.addResourceHandler(pathPatters).addResourceLocations(locations.toArray(EMPTY_STRING_ARRAY))
                .setUseLastModified(resources.getCache().isUseLastModified());
        if (cachePeriod != null) registration.setCachePeriod((int) cachePeriod.getSeconds());
        if (cacheControl != null) registration.setCacheControl(cacheControl);
        var resourceChainRegistration = registration.resourceChain(false)
                .addResolver(new DcCachingResourceResolver(cache, Math.max(maxContentLength, 1024)))
                .addTransformer(new CachingResourceTransformer(cache));
        if (resources.getChain().isCompressed()) {
            var contentCodings = environment.getProperty("dc.static.resource-content-codings", String[].class, new String[]{"gzip"});
            var encodedResourceResolver = new EncodedResourceResolver();
            encodedResourceResolver.setContentCodings(List.of(contentCodings));
            resourceChainRegistration.addResolver(encodedResourceResolver);
        }
    }

    /**
     * 大部分场景用不到这个，{@link DispatcherServlet} 已经包含了该过滤器的功能，详见{@link FrameworkServlet#initContextHolders(HttpServletRequest, LocaleContext, RequestAttributes)}和{@link FrameworkServlet#resetContextHolders(HttpServletRequest, LocaleContext, RequestAttributes)}
     */
    @ConditionalOnProperty(value = "dc.request-context-filter.remove",havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(RequestContextFilter.class)
    @Configuration(proxyBeanMethods = false)
    static final class DcWebBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            if (registry.containsBeanDefinition("requestContextFilter")) {
                registry.removeBeanDefinition("requestContextFilter");
            }
        }
    }

}
