package io.github.dengchen2020.aot.utils;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeHint;
import org.springframework.aot.hint.TypeReference;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.UnaryOperator;

/**
 * 基于spring的aot处理操作简化工具
 * @author xiaochen
 * @since 2025/5/27
 */
public class AotUtils extends CollectUtils {

    private final RuntimeHints hints;

    public static AotUtils newInstance(RuntimeHints hints, ClassLoader classLoader) {
        return new AotUtils(hints, classLoader);
    }

    public AotUtils(RuntimeHints hints, ClassLoader classLoader) {
        super(classLoader);
        this.hints = hints;
    }

    public RuntimeHints hints() {
        return hints;
    }

    public ClassLoader classLoader() {
        return classLoader;
    }

    public static final BindingReflectionHintsRegistrar REGISTRAR = new BindingReflectionHintsRegistrar();

    public void registerPattern(String... resources) {
        for (String resource : resources) {
            hints.resources().registerPattern(resource);
        }
    }

    public void registerPattern(TypeReference typeReference, String... resources) {
        for (String resource : resources) {
            hints.resources().registerPattern(builder -> builder.includes(typeReference, resource));
        }
    }

    public void registerReflectionIfPresent(UnaryOperator<TypeHint.Builder> builderUnaryOperator, String... classes) {
        for (String clazz : classes) {
            try {
                var clz = loadClass(clazz);
                if (clz != null) {
                    hints.reflection().registerType(clz, builderUnaryOperator::apply);
                }
            } catch (LinkageError ignored) {}
        }
    }

    public void registerReflectionIfPresent(String... classes) {
        for (String clazz : classes) {
            try {
                var clz = loadClass(clazz);
                if (clz != null) {
                    REGISTRAR.registerReflectionHints(hints.reflection(), clz);
                }
            } catch (LinkageError ignored) {}
        }
    }

    public void registerReflection(UnaryOperator<TypeHint.Builder> builderUnaryOperator, Collection<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                hints.reflection().registerType(clazz, builderUnaryOperator::apply);
            } catch (LinkageError ignored) {}
        }
    }

    public void registerReflection(UnaryOperator<TypeHint.Builder> builderUnaryOperator, Class<?>... classes) {
        for (Class<?> clazz : classes) {
            try {
                hints.reflection().registerType(clazz, builderUnaryOperator::apply);
            } catch (LinkageError ignored) {}
        }
    }

    public void registerReflection(UnaryOperator<TypeHint.Builder> builderUnaryOperator, String... classes) {
        for (var clazz : toClassArray(classes)) {
            try {
                hints.reflection().registerType(clazz, builderUnaryOperator::apply);
            } catch (LinkageError ignored) {}
        }
    }

    public void registerReflection(Class<?>... classes) {
        for (Class<?> clazz : classes) {
            try {
                REGISTRAR.registerReflectionHints(hints.reflection(), clazz);
            } catch (LinkageError ignored) {}
        }
    }

    public void registerReflection(Collection<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            try {
                REGISTRAR.registerReflectionHints(hints.reflection(), clazz);
            } catch (LinkageError ignored) {}
        }
    }

    public void registerReflection(String... classes) {
        for (var clazz : toClassArray(classes)) {
            try {
                REGISTRAR.registerReflectionHints(hints.reflection(), clazz);
            } catch (LinkageError ignored) {}
        }
    }

    @SafeVarargs
    public final void registerSerializable(Class<? extends Serializable>... classes) {
        for (Class<? extends Serializable> c : classes) {
            if (c.getCanonicalName() == null) continue;
            hints.reflection().registerType(c, builder -> builder.withJavaSerialization(true));
        }
    }

    public void registerSerializable(TypeReference onReachableType, Collection<Class<?>> classes) {
        for (Class<?> c : classes) {
            if (!Serializable.class.isAssignableFrom(c) || c.getCanonicalName() == null) continue;
            hints.reflection().registerType(c, builder -> builder.onReachableType(onReachableType).withJavaSerialization(true));
        }
    }

    public void registerSerializable(TypeReference onReachableType, Class<?>... classes) {
        for (Class<?> c : classes) {
            if (!Serializable.class.isAssignableFrom(c) || c.getCanonicalName() == null) continue;
            hints.reflection().registerType(c, builder -> builder.onReachableType(onReachableType).withJavaSerialization(true));
        }
    }

    public void registerSerializableIfPresent(TypeReference onReachableType, String... classes) {
        for (String cs : classes) {
            try {
                Class<?> c = classLoader.loadClass(cs);
                if (!Serializable.class.isAssignableFrom(c) || c.getCanonicalName() == null) continue;
                hints.reflection().registerType(c, builder -> builder.onReachableType(onReachableType).withJavaSerialization(true));
            } catch (ClassNotFoundException ignored) {}
        }
    }

}
