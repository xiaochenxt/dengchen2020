package io.github.dengchen2020.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仅作用于String类型字段，表示该字段的值应原样输出，不进行JSON转义 </br>
 * 适用于String类型存储JSON数据的时候使用 </br>
 * 与Jackson自带的{@link com.fasterxml.jackson.annotation.JsonRawValue}不同，该注解同时作用于输入和输出
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonRawValue
{
    /**
     * 可选参数，定义该注释是否为激活
     * 或者不。当值用于覆盖目的时，唯一的用法是“虚假”
     *（这通常不需要）;很可能是需要的
     * 带有“混合注释”（又称“注释覆盖”）。
     * 然而，在大多数情况下，默认的“true”值是完全合适的
     * 应省略。
     */
    boolean value() default true;
}
