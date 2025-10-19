package io.github.dengchen2020.core.web.mvc;

import io.github.dengchen2020.core.filter.DcShallowEtagHeaderFilter;
import io.github.dengchen2020.core.properties.DcCorsProperties;
import io.github.dengchen2020.core.properties.DcETagProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.List;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_STRING_ARRAY;

/**
 * Web自动配置
 *
 * @author xiaochen
 * @since 2025/6/23
 */
@EnableConfigurationProperties(DcCorsProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration(proxyBeanMethods = false)
public class DcWebAutoConfiguration implements WebMvcConfigurer {

    @ConditionalOnProperty(value = "dc.cors.enabled", matchIfMissing = true, havingValue = "true")
    @ConditionalOnMissingBean(value = CorsFilter.class, parameterizedContainer = FilterRegistrationBean.class)
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterFilterRegistrationBean(DcCorsProperties builder) {
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
    static class EtagAutoConfiguration {

        private final DcETagProperties properties;

        public EtagAutoConfiguration(DcETagProperties properties) {
            this.properties = properties;
        }

        @Bean
        public FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilterFilterRegistrationBean() {
            ShallowEtagHeaderFilter filter = new DcShallowEtagHeaderFilter(properties.getIgnorePath().toArray(EMPTY_STRING_ARRAY));
            if (properties.isWriteWeakETag()) filter.setWriteWeakETag(properties.isWriteWeakETag());
            FilterRegistrationBean<ShallowEtagHeaderFilter> filterRegistrationBean = new FilterRegistrationBean<>();
            filterRegistrationBean.setFilter(filter);
            return filterRegistrationBean;
        }

        @ConditionalOnMissingBean
        @Bean
        public EtagOptimizeResponseBodyAdvice etagOptimizeResponseBodyAdvice() {
            return new EtagOptimizeResponseBodyAdvice(properties.getMaxLength().toBytes());
        }
    }

}
