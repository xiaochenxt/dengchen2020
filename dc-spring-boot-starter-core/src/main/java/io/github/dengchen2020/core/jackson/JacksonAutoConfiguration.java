package io.github.dengchen2020.core.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.xml.XmlMapper;
import io.github.dengchen2020.core.utils.JsonHelper;
import io.github.dengchen2020.core.utils.XmlHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import java.util.List;

/**
 * Jackson配置选项优化自动配置
 *
 * @author xiaochen
 * @since 2024/7/17
 */
@Configuration(proxyBeanMethods = false)
public final class JacksonAutoConfiguration {

    @ConditionalOnClass(GenericJacksonJsonRedisSerializer.class)
    @Configuration(proxyBeanMethods = false)
    static final class GenericJacksonJsonRedisSerializerConfiguration {
        @ConditionalOnMissingBean
        @Bean
        GenericJacksonJsonRedisSerializer genericJacksonJsonRedisSerializer(List<JsonMapperBuilderCustomizer> customizers){
            return GenericJacksonJsonRedisSerializer.builder()
                    .enableUnsafeDefaultTyping()
                    .enableSpringCacheNullValueSupport()
                    .customize(mapper -> {
                        for (JsonMapperBuilderCustomizer customizer : customizers) customizer.customize(mapper);
                        mapper.changeDefaultPropertyInclusion(h -> h.withValueInclusion(JsonInclude.Include.NON_NULL));
                    })
                    .build();
        }
    }

    @Configuration(proxyBeanMethods = false)
    static final class JsonHelperAutoConfiguration {
        @ConditionalOnMissingBean
        @Bean
        public JsonHelper jsonHelper(JsonMapper jsonMapper) {
            return new JsonHelper(jsonMapper);
        }
    }

    @ConditionalOnClass(XmlMapper.class)
    @Configuration(proxyBeanMethods = false)
    static final class XmlHelperAutoConfiguration {
        @ConditionalOnMissingBean
        @Bean
        public XmlHelper xmlHelper(XmlMapper xmlMapper) {
            return new XmlHelper(xmlMapper);
        }
    }

}
