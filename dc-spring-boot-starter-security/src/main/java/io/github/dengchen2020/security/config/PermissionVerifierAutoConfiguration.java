package io.github.dengchen2020.security.config;

import io.github.dengchen2020.security.permission.PermissionVerifyInterceptor;
import io.github.dengchen2020.security.permission.PermissionVerifier;
import io.github.dengchen2020.security.permission.SimplePermissionVerifier;
import io.github.dengchen2020.security.properties.SecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 权限校验自动配置
 * @author xiaochen
 * @since 2024/7/22
 */
@ConditionalOnProperty(value = "dc.security.permission.enabled", havingValue = "true")
@Configuration(proxyBeanMethods = false)
public class PermissionVerifierAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public PermissionVerifier permissionVerifier() {
        return new SimplePermissionVerifier();
    }

    @Configuration(proxyBeanMethods = false)
    static class InterceptorConfiguration implements WebMvcConfigurer {

        private final SecurityProperties securityProperties;

        private final PermissionVerifier permissionVerifier;

        public InterceptorConfiguration(SecurityProperties securityProperties, PermissionVerifier permissionVerifier) {
            this.securityProperties = securityProperties;
            this.permissionVerifier = permissionVerifier;
        }

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            InterceptorRegistration registration = registry.addInterceptor(new PermissionVerifyInterceptor(permissionVerifier))
                    .order(Ordered.HIGHEST_PRECEDENCE + 10);
            if (!CollectionUtils.isEmpty(securityProperties.getResource().getPermitPath())) registration.excludePathPatterns(securityProperties.getResource().getPermitPath());
        }

    }

}
