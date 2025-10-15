package io.github.dengchen2020.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 对集合和字符串集合的非空校验，比@NotEmpty考虑更加全面
 * @author xiaochen
 * @since 2024/8/23
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {NotEmptyValidatorForCollection.class, NotEmptyValidatorForCollectionString.class})
public @interface NotEmptyCollection {

    String message() default "{jakarta.validation.constraints.NotEmpty.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
