package io.github.dengchen2020.core.validation;

import io.github.dengchen2020.core.utils.JsonUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * json字符串校验器
 * @author xiaochen
 * @since 2022/12/28
 */
public class JsonObjectOrArrayValidator implements ConstraintValidator<JsonObjectOrArray, String> {

    private int maxLength = 0;

    @Override
    public void initialize(JsonObjectOrArray jsonObjectOrArrayAnnotation) {
        maxLength = jsonObjectOrArrayAnnotation.maxLength();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        if (value.isBlank()) return false;
        if ((maxLength > 0 && value.length() > maxLength)) {
            context.buildConstraintViolationWithTemplate("{io.github.dengchen2020.core.validation.JsonObjectOrArray.dataLengthError.message}")
                    .addConstraintViolation().disableDefaultConstraintViolation();
            return false;
        }
        return JsonUtils.isJsonObjectOrArray(value);
    }

}
