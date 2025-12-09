package io.github.dengchen2020.jdbc.config;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

/**
 *
 * @author xiaochen
 * @since 2025/12/7
 */
@WritingConverter
public class PGobjectToStringConverter implements Converter<PGobject, String> {

    @Override
    public String convert(PGobject source) {
        return source.getValue();
    }
}
