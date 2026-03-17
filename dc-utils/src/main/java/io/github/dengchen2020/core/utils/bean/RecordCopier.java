package io.github.dengchen2020.core.utils.bean;

import java.beans.PropertyDescriptor;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Type;
import org.springframework.cglib.core.*;

/**
 * 基于 CGLib 字节码生成的高性能 Record 拷贝器，详见：{@link BeanCopier}
 * @author xiaochen
 * @since 2025/3/16
 */
@SuppressWarnings({"rawtypes"})
public abstract class RecordCopier<T> {

    private static final RecordCopierKey KEY_FACTORY =
            (RecordCopierKey) KeyFactory.create(RecordCopierKey.class);
    private static final Type CONVERTER =
            TypeUtils.parseType("io.github.dengchen2020.core.utils.bean.Converter");
    private static final Type RECORD_COPIER =
            TypeUtils.parseType("io.github.dengchen2020.core.utils.bean.RecordCopier");
    private static final Signature COPY =
            new Signature("copy", Constants.TYPE_OBJECT, new Type[]{Constants.TYPE_OBJECT, CONVERTER});
    private static final Signature CONVERT =
            TypeUtils.parseSignature("Object convert(Object, Class, Object, String)");

    interface RecordCopierKey {
        Object newInstance(String source, String target, boolean useConverter);
    }

    public static RecordCopier create(Class source, Class<? extends Record> target, boolean useConverter) {
        Generator gen = new Generator();
        gen.setSource(source);
        gen.setTarget(target);
        gen.setUseConverter(useConverter);
        return gen.create();
    }

    abstract public T copy(Object source, Converter converter);

    public static class Generator extends AbstractClassGenerator {
        private static final Source SOURCE = new Source(RecordCopier.class.getName());
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

        public RecordCopier create() {
            Object key = KEY_FACTORY.newInstance(source.getName(), target.getName(), useConverter);
            return (RecordCopier) super.create(key);
        }

        @Override
        protected ClassLoader getDefaultClassLoader() {
            return source.getClassLoader();
        }

        @Override
        protected ProtectionDomain getProtectionDomain() {
            return ReflectUtils.getProtectionDomain(source);
        }

        @Override
        public void generateClass(ClassVisitor v) {
            var components = target.getRecordComponents();
            var ctorArgTypes = new Type[components.length];
            for (int i = 0; i < components.length; i++) {
                ctorArgTypes[i] = Type.getType(components[i].getType());
            }

            Map<String, PropertyDescriptor> getterMap = new HashMap<>();
            if (source.isRecord()) {
                for (var rc : source.getRecordComponents()) {
                    if (!getterMap.containsKey(rc.getName())) {
                        try {
                            getterMap.put(rc.getName(), new PropertyDescriptor(rc.getName(), rc.getAccessor(), null));
                        } catch (java.beans.IntrospectionException ex) {
                            throw new IllegalArgumentException("Failed to read Record component accessor: " + rc.getName(), ex);
                        }
                    }
                }
            } else {
                var getters = ReflectUtils.getBeanGetters(source);
                for (var getter : getters) {
                    getterMap.put(getter.getName(), getter);
                }
            }
            var ctorSig = new Signature("<init>", Type.VOID_TYPE, ctorArgTypes);

            var sourceType = Type.getType(source);
            var targetType = Type.getType(target);

            var ce = new ClassEmitter(v);
            ce.begin_class(
                    Constants.V1_8,
                    Constants.ACC_PUBLIC,
                    getClassName(),
                    RECORD_COPIER,
                    null,
                    Constants.SOURCE_FILE);

            EmitUtils.null_constructor(ce);

            var e = ce.begin_method(Constants.ACC_PUBLIC, COPY, null);

            var sourceLocal = e.make_local(sourceType);
            e.load_arg(0);
            e.checkcast(sourceType);
            e.store_local(sourceLocal);

            var argLocals = new Local[components.length];
            var readSignatures = new Signature[components.length];
            var isPrimitive = new boolean[components.length];

            for (int i = 0; i < components.length; i++) {
                var componentType = components[i].getType();
                var asmType = ctorArgTypes[i];
                isPrimitive[i] = componentType.isPrimitive();
                var pd = getterMap.get(components[i].getName());

                argLocals[i] = e.make_local(asmType);
                if (isPrimitive[i]) {
                    switch (componentType.getName()) {
                        case "boolean" -> { e.push(false); e.store_local(argLocals[i]); }
                        case "byte", "int", "short" -> { e.push(0); e.store_local(argLocals[i]); }
                        case "long"    -> { e.push(0L); e.store_local(argLocals[i]); }
                        case "float"   -> { e.push(0F); e.store_local(argLocals[i]); }
                        case "double"  -> { e.push(0D); e.store_local(argLocals[i]); }
                        case "char"    -> { e.push('\0'); e.store_local(argLocals[i]); }
                    }
                } else {
                    e.aconst_null();
                    e.store_local(argLocals[i]);
                }

                if (pd != null && pd.getReadMethod() != null) {
                    readSignatures[i] = ReflectUtils.getMethodInfo(pd.getReadMethod()).getSignature();
                    var componentName = components[i].getName();

                    if (useConverter) {
                        if (isPrimitive[i]) {
                            var afterConvert = e.make_label();
                            var convertedPrim = e.make_local(Constants.TYPE_OBJECT);
                            e.load_arg(1);
                            e.load_local(sourceLocal);
                            e.invoke_virtual(sourceType, readSignatures[i]);
                            e.box(readSignatures[i].getReturnType());
                            EmitUtils.load_class(e, asmType);
                            e.push(componentName);
                            e.push(componentName);
                            e.invoke_interface(CONVERTER, CONVERT);
                            e.store_local(convertedPrim);
                            e.load_local(convertedPrim);
                            e.ifnull(afterConvert);
                            e.load_local(convertedPrim);
                            e.unbox(asmType);
                            e.store_local(argLocals[i]);
                            e.mark(afterConvert);
                        } else {
                            var skipStore = e.make_label();
                            var convertedVal = e.make_local(Constants.TYPE_OBJECT);
                            e.load_arg(1);
                            e.load_local(sourceLocal);
                            e.invoke_virtual(sourceType, readSignatures[i]);
                            EmitUtils.load_class(e, asmType);
                            e.push(componentName);
                            e.push(componentName);
                            e.invoke_interface(CONVERTER, CONVERT);
                            e.store_local(convertedVal);
                            e.load_local(convertedVal);
                            e.ifnull(skipStore);
                            e.load_local(convertedVal);
                            e.checkcast(asmType);
                            e.store_local(argLocals[i]);
                            e.mark(skipStore);
                        }
                    } else {
                        if (isPrimitive[i]) {
                            e.load_local(sourceLocal);
                            e.invoke_virtual(sourceType, readSignatures[i]);
                            e.store_local(argLocals[i]);
                        } else {
                            var skipStore = e.make_label();
                            var tmpVal = e.make_local(asmType);
                            e.load_local(sourceLocal);
                            e.invoke_virtual(sourceType, readSignatures[i]);
                            e.store_local(tmpVal);
                            e.load_local(tmpVal);
                            e.ifnull(skipStore);
                            e.load_local(tmpVal);
                            e.store_local(argLocals[i]);
                            e.mark(skipStore);
                        }
                    }
                }
            }

            e.new_instance(targetType);
            e.dup();
            for (int i = 0; i < components.length; i++) {
                e.load_local(argLocals[i]);
            }
            e.invoke_constructor(targetType, ctorSig);

            e.return_value();
            e.end_method();
            ce.end_class();
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
