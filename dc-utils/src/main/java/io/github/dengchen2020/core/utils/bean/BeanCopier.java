package io.github.dengchen2020.core.utils.bean;

import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Label;
import org.springframework.asm.Type;
import org.springframework.cglib.core.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * 扩展Spring的BeanCopier，增加更多功能，详见：{@link org.springframework.cglib.beans.BeanCopier}
 * @author xiaochen
 * @since 2025/1/6
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BeanCopier
{
    private static final BeanCopierKey KEY_FACTORY =
            (BeanCopierKey) KeyFactory.create(BeanCopierKey.class);
    private static final Type CONVERTER =
            TypeUtils.parseType("io.github.dengchen2020.core.utils.bean.Converter");
    private static final Type BEAN_COPIER =
            TypeUtils.parseType("io.github.dengchen2020.core.utils.bean.BeanCopier");
    private static final Signature COPY =
            new Signature("copy", Type.VOID_TYPE, new Type[]{ Constants.TYPE_OBJECT, Constants.TYPE_OBJECT, CONVERTER });
    private static final Signature CONVERT =
            TypeUtils.parseSignature("Object convert(Object, Class, Object, String)");

    interface BeanCopierKey {
        Object newInstance(String source, String target, boolean useConverter);
    }

    public static BeanCopier create(Class source, Class target, boolean useConverter) {
        Generator gen = new Generator();
        gen.setSource(source);
        gen.setTarget(target);
        gen.setUseConverter(useConverter);
        return gen.create();
    }

    abstract public void copy(Object from, Object to, Converter converter);

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(BeanCopier.class.getName());
        private Class source;
        private Class target;
        private boolean useConverter;

        public Generator() {
            super(SOURCE);
        }

        public void setSource(Class source) {
            this.source = source;
            // spring补丁开始
            setContextClass(source);
            setNamePrefix(source.getName());
            // spring补丁结束
        }

        public void setTarget(Class target) {
            this.target = target;
            // spring补丁开始
            setContextClass(target);
            setNamePrefix(target.getName());
            // spring补丁结束
        }

        public void setUseConverter(boolean useConverter) {
            this.useConverter = useConverter;
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return source.getClassLoader();
        }

        @Override
        protected ProtectionDomain getProtectionDomain() {
            return ReflectUtils.getProtectionDomain(source);
        }

        public BeanCopier create() {
            Object key = KEY_FACTORY.newInstance(source.getName(), target.getName(), useConverter);
            return (BeanCopier)super.create(key);
        }

        @Override
        public void generateClass(ClassVisitor v) {
            Type sourceType = Type.getType(source);
            Type targetType = Type.getType(target);
            ClassEmitter ce = new ClassEmitter(v);
            ce.begin_class(Constants.V1_8,
                    Constants.ACC_PUBLIC,
                    getClassName(),
                    BEAN_COPIER,
                    null,
                    Constants.SOURCE_FILE);

            EmitUtils.null_constructor(ce);
            CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, COPY, null);
            PropertyDescriptor[] getters = ReflectUtils.getBeanGetters(source);
            PropertyDescriptor[] setters = ReflectUtils.getBeanSetters(target);

            Map names = new HashMap();
            for (PropertyDescriptor getter : getters) {
                names.put(getter.getName(), getter);
            }
            Local targetLocal = e.make_local();
            Local sourceLocal = e.make_local();
            if (useConverter) {
                e.load_arg(1);
                e.checkcast(targetType);
                e.store_local(targetLocal);
                e.load_arg(0);
                e.checkcast(sourceType);
                e.store_local(sourceLocal);
            } else {
                e.load_arg(1);
                e.checkcast(targetType);
                e.load_arg(0);
                e.checkcast(sourceType);
            }
            for (PropertyDescriptor setter : setters) {
                PropertyDescriptor getter = (PropertyDescriptor)names.get(setter.getName());
                if (getter != null) {
                    Method getterMethod = getter.getReadMethod();
                    MethodInfo read = ReflectUtils.getMethodInfo(getterMethod);
                    MethodInfo write = ReflectUtils.getMethodInfo(setter.getWriteMethod());
                    Signature readSignature = read.getSignature();
                    Signature writeSignature = write.getSignature();
                    boolean isPrimitive = getterMethod.getReturnType().isPrimitive();
                    if (useConverter) {
                        e.load_local(sourceLocal);
                        Label nonNull = null;
                        if (!isPrimitive) {
                            nonNull = new Label();
                            e.dup();
                            e.invoke_virtual(sourceType, readSignature);
                            e.ifnull(nonNull);
                        }

                        Type setterType = writeSignature.getArgumentTypes()[0];
                        e.load_local(targetLocal);
                        e.load_arg(2);
                        e.load_local(sourceLocal);
                        e.invoke_virtual(sourceType, readSignature);
                        e.box(readSignature.getReturnType());
                        EmitUtils.load_class(e, setterType);
                        e.push(writeSignature.getName());
                        e.push(setter.getName());
                        e.invoke_interface(CONVERTER, CONVERT);
                        e.unbox_or_zero(setterType);
                        e.invoke_virtual(targetType, writeSignature);

                        if (!isPrimitive) e.mark(nonNull);
                    } else if (compatible(getter, setter)) {
                        Label nonNull = null;
                        if (!isPrimitive) {
                            nonNull = new Label();
                            e.dup();
                            e.invoke_virtual(sourceType, readSignature);
                            e.ifnull(nonNull);
                        }

                        e.dup2();
                        e.invoke_virtual(sourceType, readSignature);
                        e.invoke_virtual(targetType, writeSignature);

                        if (!isPrimitive) e.mark(nonNull);
                    }
                }
            }
            e.return_value();
            e.end_method();
            ce.end_class();
        }

        private static boolean compatible(PropertyDescriptor getter, PropertyDescriptor setter) {
            // TODO: 允许自动扩大转换？
            return setter.getPropertyType().isAssignableFrom(getter.getPropertyType());
        }

        @Override
        protected Object firstInstance(Class type) {
            return ReflectUtils.newInstance(type);
        }

        @Override
        protected Object nextInstance(Object instance) {
            return instance;
        }

    }
}
