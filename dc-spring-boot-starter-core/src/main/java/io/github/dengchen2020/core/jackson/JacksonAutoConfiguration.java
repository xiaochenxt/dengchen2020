package io.github.dengchen2020.core.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.github.dengchen2020.core.utils.JsonHelper;
import io.github.dengchen2020.core.utils.XmlHelper;
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
public final class JacksonAutoConfiguration {

    @ConditionalOnClass(GenericJackson2JsonRedisSerializer.class)
    @Configuration(proxyBeanMethods = false)
    static final class GenericJackson2JsonRedisSerializerConfiguration {
        @ConditionalOnMissingBean
        @Bean
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder){
            return GenericJackson2JsonRedisSerializer.builder()
                    .defaultTyping(true)
                    .objectMapper(jackson2ObjectMapperBuilder.createXmlMapper(false)
                            .failOnUnknownProperties(false)
                            .serializationInclusion(JsonInclude.Include.NON_NULL)
                            .build()
                    )
                    .build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static final class JsonHelperAutoConfiguration {
        @ConditionalOnMissingBean
        @Bean
        public JsonHelper jsonHelper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
            var mapper = new JsonMapper();
            jackson2ObjectMapperBuilder.configure(mapper);
            return new JsonHelper(mapper);
        }
    }

    @ConditionalOnClass(XmlMapper.class)
    @Configuration(proxyBeanMethods = false)
    static final class XmlHelperAutoConfiguration {
        @ConditionalOnMissingBean
        @Bean
        public XmlHelper xmlHelper(Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
            var mapper = new XmlMapper();
            jackson2ObjectMapperBuilder.configure(mapper);
            return new XmlHelper(mapper);
        }
    }

}
