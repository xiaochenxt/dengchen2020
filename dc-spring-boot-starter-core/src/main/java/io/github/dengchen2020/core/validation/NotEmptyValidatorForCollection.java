package io.github.dengchen2020.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;

/**
 * 对集合非空校验的增强
 *
 * @author xiaochen
 * @since 2024/8/23
 */
@SuppressWarnings("rawtypes")
public class NotEmptyValidatorForCollection implements ConstraintValidator<NotEmptyCollection, Collection> {

    @Override
    public boolean isValid(Collection collection, ConstraintValidatorContext constraintValidatorContext) {
        if (collection == null || collection.isEmpty()) return false;
        for (Object o : collection) {
            if (o == null) return false;
        }
        return true;
    }

}
