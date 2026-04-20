package io.github.dengchen2020.jdbc.base;

import com.querydsl.core.types.EntityPath;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 生成querydsl-sql所需的D类
 * @author xiaochen
 * @since 2026/4/18
 */
public class DcSqlEntityPathResolver extends SimpleEntityPathResolver  {

    public static final DcSqlEntityPathResolver INSTANCE = new DcSqlEntityPathResolver("");

    private static final String NO_CLASS_FOUND_TEMPLATE = "Did not find a query class %s for domain class %s";
    private static final String NO_FIELD_FOUND_TEMPLATE = "Did not find a static field of the same type in %s";

    public DcSqlEntityPathResolver(String querySuffix) {
        super(querySuffix);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> EntityPath<T> createPath(Class<T> domainClass) {

        String pathClassName = getQueryClassName(domainClass);

        try {

            Class<?> pathClass = ClassUtils.forName(pathClassName, domainClass.getClassLoader());

            return getStaticFieldOfType(pathClass)//
                    .map(it -> (EntityPath<T>) ReflectionUtils.getField(it, null))//
                    .orElseThrow(() -> new IllegalStateException(String.format(NO_FIELD_FOUND_TEMPLATE, pathClass)));

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format(NO_CLASS_FOUND_TEMPLATE, pathClassName, domainClass.getName()),
                    e);
        }
    }

    private String getQueryClassName(Class<?> domainClass) {

        String simpleClassName = ClassUtils.getShortName(domainClass);
        String packageName = domainClass.getPackage().getName();

        return String.format("%s%s.D%s%s", packageName, "", getClassBase(simpleClassName),
                domainClass.getSimpleName());
    }

    private Optional<Field> getStaticFieldOfType(Class<?> type) {

        for (Field field : type.getDeclaredFields()) {

            boolean isStatic = Modifier.isStatic(field.getModifiers());
            boolean hasSameType = type.equals(field.getType());

            if (isStatic && hasSameType) {
                return Optional.of(field);
            }
        }

        Class<?> superclass = type.getSuperclass();
        return Object.class.equals(superclass) ? Optional.empty() : getStaticFieldOfType(superclass);
    }

    private String getClassBase(String shortName) {

        String[] parts = shortName.split("\\.");

        return parts.length < 2 ? "" : parts[0] + "_";
    }

}
