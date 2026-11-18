package io.github.dengchen2020.core.jsonb;

import io.avaje.jsonb.Jsonb;
import io.github.dengchen2020.core.utils.jsonb.LocalDateTimeAdapter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;

/**
 * Jsonb自动配置
 *
 * @author xiaochen
 * @since 2024/7/17
 */
@ConditionalOnClass(Jsonb.class)
@Configuration(proxyBeanMethods = false)
public final class JsonbAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    Jsonb jsonb(Environment environment) {
        boolean failUnknown = environment.getProperty("dc.jsonb.deserialize.failOnUnknown", Boolean.class, false);
        boolean failOnNullPrimitives = environment.getProperty("dc.jsonb.deserialize.failOnNullPrimitives", Boolean.class, false);
        boolean mathTypesAsString = environment.getProperty("dc.jsonb.serialize.mathTypesAsString", Boolean.class, false);
        boolean serializeEmpty = environment.getProperty("dc.jsonb.serialize.empty", Boolean.class, true);
        boolean serializeNulls = environment.getProperty("dc.jsonb.serialize.nulls", Boolean.class, false);
        var dateFormat = environment.getProperty("spring.mvc.format.date-time");
        var jsonb = Jsonb.builder()
                .failOnUnknown(failUnknown)
                .failOnNullPrimitives(failOnNullPrimitives)
                .mathTypesAsString(mathTypesAsString)
                .serializeEmpty(serializeEmpty)
                .serializeNulls(serializeNulls)
                .add(LocalDateTime.class, LocalDateTimeAdapter.create(dateFormat))
                .build();
        return jsonb;
    }

}
