package io.github.dengchen2020.security.config;

import io.fusionauth.jwt.domain.Header;
import io.fusionauth.jwt.domain.JWT;
import io.fusionauth.jwt.json.ZonedDateTimeDeserializer;
import io.fusionauth.jwt.json.ZonedDateTimeSerializer;
import io.github.dengchen2020.aot.utils.AotUtils;
import io.github.dengchen2020.security.authentication.token.AuthenticationConvert;
import io.github.dengchen2020.security.authentication.token.JwtTokenService;
import io.github.dengchen2020.security.authentication.token.RedisSimpleTokenService;
import io.github.dengchen2020.security.authentication.token.RedisTokenService;
import io.github.dengchen2020.security.properties.SecurityProperties;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

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

    @ImportRuntimeHints(JwtToken.JWTRuntimeHintsRegistrar.class)
    @ConditionalOnClass(JWT.class)
    @ConditionalOnProperty(value = "dc.security.jwt.secret")
    @Configuration(proxyBeanMethods = false)
    final static class JwtToken {
        @ConditionalOnMissingBean
        @Bean
        JwtTokenService jwtTokenService(SecurityProperties securityProperties) {
            SecurityProperties.JWT jwt = securityProperties.getJwt();
            return new JwtTokenService(jwt.getSecret(), jwt.getExpireIn().toSeconds(), jwt.getRefreshExpireIn().toSeconds(), securityProperties.getTokenName());
        }

        static class JWTRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

            @Override
            public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
                var aotUtils = new AotUtils(hints, classLoader);
                aotUtils.registerReflection(JWT.class, Header.class,
                        ZonedDateTimeSerializer.class, ZonedDateTimeDeserializer.class);
            }

        }

    }

    @ConditionalOnProperty(value = "dc.security.simple-token.expire-in")
    @Configuration(proxyBeanMethods = false)
    final static class SimpleToken{
        @ConditionalOnMissingBean
        @Bean
        RedisSimpleTokenService simpleTokenService(SecurityProperties securityProperties) {
            SecurityProperties.SimpleToken simpleToken = securityProperties.getSimpleToken();
            return new RedisSimpleTokenService(simpleToken.getExpireIn().toSeconds(), simpleToken.getDevice(), simpleToken.isAutorenewal(), simpleToken.getAutorenewalSeconds(), securityProperties.getTokenName());
        }
    }

    @ConditionalOnProperty(value = "dc.security.token.expire-in")
    @Configuration(proxyBeanMethods = false)
    final static class Token {
        @ConditionalOnMissingBean
        @Bean
        RedisTokenService tokenService(SecurityProperties securityProperties) {
            SecurityProperties.Token token = securityProperties.getToken();
            return new RedisTokenService(token.getExpireIn().toSeconds(), token.getMaxOnlineNum(), token.getDevice(), token.isAutorenewal(), token.getAutorenewalSeconds(), securityProperties.getTokenName());
        }
    }

}
