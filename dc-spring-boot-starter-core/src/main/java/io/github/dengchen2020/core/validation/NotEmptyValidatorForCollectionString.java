package io.github.dengchen2020.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * 对字符串集合非空校验的增强
 *
 * @author xiaochen
 * @since 2024/8/23
 */
public class NotEmptyValidatorForCollectionString implements ConstraintValidator<NotEmptyCollection, Collection<String>> {

    @Override
    public boolean isValid(Collection<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return false;
        for (String s : value) {
            if (!StringUtils.hasText(s)) return false;
        }
        return true;
    }

}
