package io.github.dengchen2020.core.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson配置选项优化自动配置
 *
 * @author xiaochen
 * @since 2024/7/17
 */
@Configuration(proxyBeanMethods = false)
public class JacksonAutoConfiguration {

    @ConditionalOnClass(GenericJackson2JsonRedisSerializer.class)
    @Configuration(proxyBeanMethods = false)
    static class GenericJackson2JsonRedisSerializerConfiguration {
        @ConditionalOnMissingBean
        @Bean
        public GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder){
            return GenericJackson2JsonRedisSerializer.builder()
                    .defaultTyping(true)
                    .objectMapper(jackson2ObjectMapperBuilder.createXmlMapper(false).build()
                            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                    )
                    .build();
        }
    }

}
