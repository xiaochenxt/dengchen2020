package io.github.dengchen2020.core.utils;

import org.jspecify.annotations.NonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static io.github.dengchen2020.core.utils.EmptyConstant.EMPTY_CLASS_ARRAY;

/**
 * MethodHandle操作简化工具类
 * <p>因为类型擦除等原因，工具类中无法使用{@code invokeExact}（性能极好，但严格要求类型一致）和{@code invoke}（性能极好，比{@code invokeExact}性能差一点点，但使用更方便，不严格要求类型一致），使用示例：
 * <pre>
 * {@code
 * private String name;
 * public String getName() {
 *     return this.name;
 * }
 * }
 * {@code
 * var dto = new DTO();
 * MethodHandle methodHandle = MethodHandleUtils.getPublicMethod(DTO.class, "getName", String.class);
 * String name = (String) methodHandle.invokeExact(dto)
 * }
 * </pre>
 * </p>
 * <p>
 * 拒绝使用{@code invokeWithArguments}，性能比反射还差很多倍
 * </p>
 * @author xiaochen
 * @since 2025/9/23
 */
public abstract class MethodHandleUtils {

    /**
     * 获取公共实例方法的MethodHandle（无参数，返回值为void）
     * @param clazz 目标类
     * @param methodName 方法名
     * @return 方法句柄
     */
    public static MethodHandle getPublicMethod(Class<?> clazz, String methodName) {
        return getPublicMethod(clazz, methodName, void.class);
    }

    /**
     * 获取公共实例方法的MethodHandle（无参数，指定返回值类型）
     * @param clazz 目标类
     * @param methodName 方法名
     * @param returnType 返回值类型
     * @return 方法句柄
     */
    public static MethodHandle getPublicMethod(Class<?> clazz, String methodName, Class<?> returnType) {
        return getPublicMethod(clazz, methodName, returnType, EMPTY_CLASS_ARRAY);
    }

    /**
     * 获取公共实例方法的MethodHandle（带参数）
     * @param clazz 目标类
     * @param methodName 方法名
     * @param returnType 返回值类型
     * @param paramTypes 参数类型数组
     * @return 方法句柄
     */
    public static MethodHandle getPublicMethod(@NonNull Class<?> clazz,@NonNull String methodName,
                                               @NonNull Class<?> returnType,@NonNull Class<?>... paramTypes) {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        MethodType methodType = MethodType.methodType(returnType, paramTypes);
        try {
            return lookup.findVirtual(clazz, methodName, methodType);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("未找到public方法: " + clazz.getName() + "." + methodName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问public方法: " + clazz.getName() + "." + methodName, e);
        }
    }

    /**
     * 获取私有/受保护实例方法的MethodHandle（无参数）
     * @param clazz 目标类
     * @param methodName 方法名
     * @param returnType 返回值类型
     * @return 方法句柄
     */
    public static MethodHandle getPrivateMethod(Class<?> clazz, String methodName, Class<?> returnType) {
        return getPrivateMethod(clazz, methodName, returnType, EMPTY_CLASS_ARRAY);
    }

    /**
     * 获取私有/受保护实例方法的MethodHandle（带参数）
     * @param clazz 目标类
     * @param methodName 方法名
     * @param returnType 返回值类型
     * @param paramTypes 参数类型数组
     * @return 方法句柄
     */
    public static MethodHandle getPrivateMethod(@NonNull Class<?> clazz,@NonNull String methodName,
                                                @NonNull Class<?> returnType,@NonNull Class<?>... paramTypes) {
        MethodHandles.Lookup lookup;
        try {
            lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法获取方法访问权限: " + clazz.getName(), e);
        }
        MethodType methodType = MethodType.methodType(returnType, paramTypes);
        try {
            return lookup.findVirtual(clazz, methodName, methodType);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("未找到方法: " + clazz.getName() + "." + methodName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问方法: " + clazz.getName() + "." + methodName, e);
        }
    }

    /**
     * 获取公共静态方法的MethodHandle（无参数）
     * @param clazz 目标类
     * @param methodName 方法名
     * @param returnType 返回值类型
     * @return 方法句柄
     */
    public static MethodHandle getPublicStaticMethod(Class<?> clazz, String methodName, Class<?> returnType) {
        return getPublicStaticMethod(clazz, methodName, returnType, EMPTY_CLASS_ARRAY);
    }

    /**
     * 获取公共静态方法的MethodHandle（带参数）
     * @param clazz 目标类
     * @param methodName 方法名
     * @param returnType 返回值类型
     * @param paramTypes 参数类型数组
     * @return 方法句柄
     */
    public static MethodHandle getPublicStaticMethod(@NonNull Class<?> clazz,@NonNull String methodName,
                                                     @NonNull Class<?> returnType,@NonNull Class<?>... paramTypes) {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        MethodType methodType = MethodType.methodType(returnType, paramTypes);
        try {
            return lookup.findStatic(clazz, methodName, methodType);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("未找到public静态方法: " + clazz.getName() + "." + methodName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问public静态方法: " + clazz.getName() + "." + methodName, e);
        }
    }

    /**
     * 获取私有/受保护静态方法的MethodHandle（无参数）
     * @param clazz 目标类
     * @param methodName 方法名
     * @param returnType 返回值类型
     * @return 方法句柄
     */
    public static MethodHandle getPrivateStaticMethod(Class<?> clazz, String methodName, Class<?> returnType) {
        return getPrivateStaticMethod(clazz, methodName, returnType, EMPTY_CLASS_ARRAY);
    }

    /**
     * 获取私有/受保护静态方法的MethodHandle（带参数）
     * @param clazz 目标类
     * @param methodName 方法名
     * @param returnType 返回值类型
     * @param paramTypes 参数类型数组
     * @return 方法句柄
     */
    public static MethodHandle getPrivateStaticMethod(@NonNull Class<?> clazz,@NonNull String methodName,
                                                      @NonNull Class<?> returnType,@NonNull Class<?>... paramTypes) {
        MethodHandles.Lookup lookup;
        try {
            lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法获取静态方法访问权限: " + clazz.getName(), e);
        }
        MethodType methodType = MethodType.methodType(returnType, paramTypes);
        try {
            return lookup.findStatic(clazz, methodName, methodType);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("未找到静态方法: " + clazz.getName() + "." + methodName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问静态方法: " + clazz.getName() + "." + methodName, e);
        }
    }

    /**
     * 获取公共实例字段的VarHandle
     * @param clazz 目标类
     * @param fieldName 字段名
     * @return 变量句柄
     */
    public static VarHandle getPublicField(@NonNull Class<?> clazz,@NonNull String fieldName, Class<?> type) {
        try {
            return MethodHandles.publicLookup().findVarHandle(clazz, fieldName, type);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("未找到public字段: " + clazz.getName() + "." + fieldName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问public字段: " + clazz.getName() + "." + fieldName, e);
        }
    }

    /**
     * 获取静态公共字段的VarHandle
     * @param clazz 目标类
     * @param fieldName 字段名
     * @return 变量句柄
     */
    public static VarHandle getPublicStaticField(@NonNull Class<?> clazz,@NonNull String fieldName, Class<?> type) {
        try {
            return MethodHandles.publicLookup().findStaticVarHandle(clazz, fieldName, type);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("未找到"+type+" public静态字段: " + clazz.getName() + "." + fieldName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问"+type+" public静态字段: " + clazz.getName() + "." + fieldName, e);
        }
    }

    /**
     * 获取私有/受保护实例字段的VarHandle
     * @param clazz 目标类
     * @param fieldName 字段名
     * @return 变量句柄
     */
    public static VarHandle getPrivateField(@NonNull Class<?> clazz,@NonNull String fieldName, @NonNull Class<?> type) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
            return lookup.findVarHandle(clazz, fieldName, type);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("未找到"+type+"字段: " + clazz.getName() + "." + fieldName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问"+type+"字段: " + clazz.getName() + "." + fieldName, e);
        }
    }

    /**
     * 获取静态私有/受保护字段的VarHandle
     * @param clazz 目标类
     * @param fieldName 字段名
     * @return 变量句柄
     */
    public static VarHandle getPrivateStaticField(@NonNull Class<?> clazz,@NonNull String fieldName, @NonNull Class<?> type) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
            return lookup.findStaticVarHandle(clazz, fieldName, type);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("未找到"+type+"静态字段: " + clazz.getName() + "." + fieldName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法访问"+type+"静态字段: " + clazz.getName() + "." + fieldName, e);
        }
    }

    /**
     * 读取实例字段值
     * @param varHandle 变量句柄
     * @param instance 实例对象
     * @return 字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(@NonNull VarHandle varHandle,@NonNull Object instance) {
        try {
            return (T) varHandle.get(instance);
        } catch (Throwable e) {
            throw new IllegalArgumentException("读取字段失败：" + varHandle, e);
        }
    }

    /**
     * 设置实例字段值
     * @param varHandle 变量句柄
     * @param instance 实例对象
     * @param value 要设置的值
     */
    public static void setFieldValue(@NonNull VarHandle varHandle,@NonNull Object instance,@NonNull Object value) {
        try {
            varHandle.set(instance, value);
        } catch (Throwable e) {
            throw new IllegalArgumentException("设置字段失败：" + varHandle, e);
        }
    }

    /**
     * 读取静态字段值
     * @param varHandle 变量句柄
     * @return 字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(@NonNull VarHandle varHandle) {
        try {
            return (T) varHandle.get();
        } catch (Throwable e) {
            throw new IllegalArgumentException("读取静态字段失败：" + varHandle, e);
        }
    }

    /**
     * 设置静态字段值
     * @param varHandle 变量句柄
     * @param value 要设置的值
     */
    public static void setFieldValue(@NonNull VarHandle varHandle,@NonNull Object value) {
        try {
            varHandle.set(value);
        } catch (Throwable e) {
            throw new IllegalArgumentException("设置静态字段失败：" + varHandle, e);
        }
    }

    /**
     * 将Method转换为MethodHandle
     * @param method 反射方法对象
     * @return 方法句柄
     */
    public static MethodHandle unreflect(@NonNull Method method) {
        try {
            if (Modifier.isPublic(method.getModifiers())) {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                return lookup.unreflect(method);
            } else {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                return MethodHandles.privateLookupIn(method.getDeclaringClass(), lookup)
                        .unreflect(method);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法转换方法为MethodHandle：" + method, e);
        }
    }

    /**
     * 将Field转换为MethodHandle
     * @param field 反射方法对象
     * @return 方法句柄
     */
    public static MethodHandle unreflectGetter(@NonNull Field field) {
        try {
            if (Modifier.isPublic(field.getModifiers())) {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                return lookup.unreflectGetter(field);
            } else {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                return MethodHandles.privateLookupIn(field.getDeclaringClass(), lookup)
                        .unreflectGetter(field);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法转换字段为MethodHandle：" + field, e);
        }
    }

    /**
     * 将Field转换为MethodHandle
     * @param field 反射方法对象
     * @return 方法句柄
     */
    public static MethodHandle unreflectSetter(@NonNull Field field) {
        try {
            if (Modifier.isPublic(field.getModifiers())) {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                return lookup.unreflectSetter(field);
            } else {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                return MethodHandles.privateLookupIn(field.getDeclaringClass(), lookup)
                        .unreflectSetter(field);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法转换字段为MethodHandle：" + field, e);
        }
    }

    /**
     * 将Constructor转换为MethodHandle
     * @param constructor 反射构造函数对象
     * @return 方法句柄
     */
    public static MethodHandle unreflectConstructor(@NonNull Constructor<?> constructor) {
        try {
            if (Modifier.isPublic(constructor.getModifiers())) {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                return lookup.unreflectConstructor(constructor);
            } else {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                return MethodHandles.privateLookupIn(constructor.getDeclaringClass(), lookup)
                        .unreflectConstructor(constructor);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法转换构造函数为MethodHandle：" + constructor, e);
        }
    }

    /**
     * 将Field转换为VarHandle
     * @param field 反射字段对象
     * @return 变量句柄
     */
    public static VarHandle unreflectVarHandle(@NonNull Field field) {
        try {
            if (Modifier.isPublic(field.getModifiers())) {
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                return lookup.unreflectVarHandle(field);
            } else {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                return MethodHandles.privateLookupIn(field.getDeclaringClass(), lookup)
                        .unreflectVarHandle(field);
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("无法转换字段为VarHandle：" + field, e);
        }
    }

}
