package io.github.dengchen2020.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 在仅对非null值修改的场景中，只需要校验是不是空字符串
 * @author xiaochen
 * @since 2025/3/17
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {NotEmptyAllowNullValidatorForString.class})
public @interface NotEmptyAllowNull {

    String message() default "{io.github.dengchen2020.core.validation.NotEmptyAllowNull.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
