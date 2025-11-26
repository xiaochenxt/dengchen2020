package io.github.dengchen2020.security.config;

import io.github.dengchen2020.security.authentication.filter.AuthenticationFilter;
import io.github.dengchen2020.security.authentication.interceptor.AuthenticationInterceptor;
import io.github.dengchen2020.security.authentication.token.TokenService;
import io.github.dengchen2020.security.event.listener.SecurityScheduledTaskHandleListener;
import io.github.dengchen2020.security.properties.SecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.filter.OrderedFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Security过滤拦截自动配置
 * @author xiaochen
 * @since 2024/4/25
 */
@ConditionalOnBean(TokenService.class)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration implements WebMvcConfigurer {

    private final TokenService tokenService;

    private final SecurityProperties securityProperties;

    public SecurityAutoConfiguration(TokenService tokenService, SecurityProperties securityProperties) {
        this.tokenService = tokenService;
        this.securityProperties = securityProperties;
    }

    @ConditionalOnMissingBean
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    @ConditionalOnMissingBean(value = AuthenticationFilter.class, parameterizedContainer = FilterRegistrationBean.class)
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilter() {
        FilterRegistrationBean<AuthenticationFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new AuthenticationFilter(tokenService));
        filterRegistrationBean.setOrder(OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 300);
        return filterRegistrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration registration = registry.addInterceptor(new AuthenticationInterceptor())
                .order(Ordered.HIGHEST_PRECEDENCE);
        if (!CollectionUtils.isEmpty(securityProperties.getResource().getPermitPath())) registration.excludePathPatterns(securityProperties.getResource().getPermitPath());
    }

    @Bean
    public SecurityScheduledTaskHandleListener securityScheduledTaskHandleListener(){
        return new SecurityScheduledTaskHandleListener();
    }

}
