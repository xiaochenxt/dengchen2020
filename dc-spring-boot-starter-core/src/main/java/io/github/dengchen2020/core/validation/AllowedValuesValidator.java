package io.github.dengchen2020.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * 可选值校验器
 * @author xiaochen
 * @since 2022/12/28
 */
public class AllowedValuesValidator implements ConstraintValidator<AllowedValues,String> {

    private Set<String> v = null;

    @Override
    public void initialize(AllowedValues constraintAnnotation) {
        v = Set.of(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || v.contains(value);
    }

}
