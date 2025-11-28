package io.github.dengchen2020.security.config;

import io.github.dengchen2020.security.authentication.token.AuthenticationConvert;
import io.github.dengchen2020.security.authentication.token.JwtTokenServiceImpl;
import io.github.dengchen2020.security.authentication.token.SimpleTokenServiceImpl;
import io.github.dengchen2020.security.authentication.token.TokenServiceImpl;
import io.github.dengchen2020.security.properties.SecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Token认证实现类自动配置
 * @author xiaochen
 * @since 2025/11/28
 */
@EnableConfigurationProperties(SecurityProperties.class)
@ConditionalOnProperty("dc.security.authentication-type")
@Configuration(proxyBeanMethods = false)
public final class TokenAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    AuthenticationConvert authenticationConvert(SecurityProperties securityProperties) {
        return securityProperties::getAuthenticationType;
    }

    @ConditionalOnProperty(value = "dc.security.jwt.secret")
    @Configuration(proxyBeanMethods = false)
    final static class JwtToken {
        @ConditionalOnMissingBean
        @Bean
        JwtTokenServiceImpl jwtTokenService(SecurityProperties securityProperties, AuthenticationConvert authenticationConvert) {
            SecurityProperties.JWT jwt = securityProperties.getJwt();
            return new JwtTokenServiceImpl(jwt.getSecret(), authenticationConvert, jwt.getExpireIn().toSeconds(), jwt.getRefreshExpireIn().toSeconds());
        }
    }

    @ConditionalOnProperty(value = "dc.security.simple-token.expire-in")
    @Configuration(proxyBeanMethods = false)
    final static class SimpleToken{
        @ConditionalOnMissingBean
        @Bean
        SimpleTokenServiceImpl simpleTokenService(StringRedisTemplate stringRedisTemplate, SecurityProperties securityProperties, AuthenticationConvert authenticationConvert) {
            SecurityProperties.SimpleToken simpleToken = securityProperties.getSimpleToken();
            return new SimpleTokenServiceImpl(stringRedisTemplate, authenticationConvert, simpleToken.getExpireIn().toSeconds(), simpleToken.getDevice(), simpleToken.isAutorenewal(), simpleToken.getAutorenewalSeconds());
        }
    }

    @ConditionalOnProperty(value = "dc.security.token.expire-in")
    @Configuration(proxyBeanMethods = false)
    final static class Token {
        @ConditionalOnMissingBean
        @Bean
        TokenServiceImpl tokenService(StringRedisTemplate stringRedisTemplate,SecurityProperties securityProperties, AuthenticationConvert authenticationConvert) {
            SecurityProperties.Token token = securityProperties.getToken();
            return new TokenServiceImpl(stringRedisTemplate, authenticationConvert, token.getExpireIn().toSeconds(), token.getMaxOnlineNum(), token.getDevice(), token.isAutorenewal(), token.getAutorenewalSeconds());
        }
    }

}
