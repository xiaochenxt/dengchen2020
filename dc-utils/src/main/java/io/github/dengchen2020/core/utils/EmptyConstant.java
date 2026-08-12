package io.github.dengchen2020.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 定义空属性常量，提高可读性并减少不必要的内存分配
 * @author xiaochen
 * @since 2025/10/18
 */
public final class EmptyConstant {

    private EmptyConstant() {}

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    public static final String EMPTY_STRING = "";

    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];

    public static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

    public static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

}
