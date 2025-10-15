package io.github.dengchen2020.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 可选值
 * @author xiaochen
 * @since 2022/12/28
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {AllowedValuesValidator.class})
public @interface AllowedValues {

    String[] value();

    String message() default "{validation.io.github.dengchen2020.core.validation.AllowedValues.message}{value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
