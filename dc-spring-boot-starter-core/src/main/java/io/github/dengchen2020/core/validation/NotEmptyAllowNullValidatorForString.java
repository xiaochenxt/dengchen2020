package io.github.dengchen2020.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 在仅对非null值修改的场景中，只需要校验是不是空字符串
 *
 * @author xiaochen
 * @since 2024/8/23
 */
public class NotEmptyAllowNullValidatorForString implements ConstraintValidator<NotEmptyAllowNull, CharSequence> {

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
        return value == null || !value.isEmpty();
    }

}
