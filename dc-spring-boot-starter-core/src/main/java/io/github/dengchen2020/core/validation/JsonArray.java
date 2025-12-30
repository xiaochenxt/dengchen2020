package io.github.dengchen2020.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 是否是json
 * @author xiaochen
 * @since 2022/12/28
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {JsonArrayValidator.class})
public @interface JsonArray {

    String message() default "{io.github.dengchen2020.core.validation.JsonArray.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 限制json数据的长度，0为不限制
     * @return
     */
    int maxLength() default 0;

}
