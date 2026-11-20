package io.github.dengchen2020.aot;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.ClassReader;
import org.springframework.asm.ClassVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 对启动类所在包及子包下所有符合 JavaBeans 规范的类和 Record 类注册反射 hints。
 */
public class JavaBeansHintsAotProcessor implements BeanFactoryInitializationAotProcessor {

    private final BindingReflectionHintsRegistrar registrar = new BindingReflectionHintsRegistrar();

    @Override
    public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
        var basePackages = findBasePackages(beanFactory);
        if (basePackages.isEmpty()) {
            return null;
        }
        var candidates = scanCandidates(beanFactory.getBeanClassLoader(), basePackages);
        return candidates.isEmpty() ? null : new JavaBeansContribution(candidates);
    }

    private static Set<String> findBasePackages(ConfigurableListableBeanFactory beanFactory) {
        var basePackages = new LinkedHashSet<String>();
        for (var beanName : beanFactory.getBeanNamesForAnnotation(SpringBootConfiguration.class)) {
            var beanType = beanFactory.getType(beanName);
            if (beanType != null) {
                basePackages.add(ClassUtils.getPackageName(ClassUtils.getUserClass(beanType)));
            }
        }
        if (basePackages.isEmpty()) {
            try {
                basePackages.addAll(AutoConfigurationPackages.get(beanFactory));
            }
            catch (IllegalStateException ignored) {
                // 未启用自动配置时没有可扫描的包
            }
        }
        return basePackages;
    }

    private static Set<Class<?>> scanCandidates(ClassLoader classLoader, Set<String> basePackages) {
        var resolver = new PathMatchingResourcePatternResolver(classLoader);
        var readerFactory = new SimpleMetadataReaderFactory(classLoader);
        var candidates = new LinkedHashSet<Class<?>>();
        for (var basePackage : basePackages) {
            var pattern = "classpath*:" + ClassUtils.convertClassNameToResourcePath(basePackage) + "/**/*.class";
            Resource[] resources;
            try {
                resources = resolver.getResources(pattern);
            }
            catch (IOException ex) {
                throw new IllegalStateException("Failed to scan package: " + basePackage, ex);
            }
            for (var resource : resources) {
                var candidate = resolveCandidate(readerFactory, resource, classLoader);
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
        }
        return candidates;
    }

    private static Class<?> resolveCandidate(SimpleMetadataReaderFactory readerFactory, Resource resource,
                                                       ClassLoader classLoader) {
        MetadataReader metadataReader;
        try {
            metadataReader = readerFactory.getMetadataReader(resource);
        }
        catch (IOException ex) {
            return null;
        }
        ClassMetadata metadata = metadataReader.getClassMetadata();
        if (!metadata.isIndependent() || metadata.isInterface() || metadata.isAnnotation() || metadata.isAbstract()) {
            return null;
        }
        Class<?> type;
        try {
            type = ClassUtils.forName(metadata.getClassName(), classLoader);
        }
        catch (ClassNotFoundException | LinkageError ex) {
            return null;
        }
        if (AnnotatedElementUtils.hasAnnotation(type, Component.class) || hasGeneratedAnnotation(resource)) {
            return null;
        }
        return (type.isRecord() || isJavaBean(type)) ? type : null;
    }

    /**
     * 通过ASM读取类上的Generated注解（含CLASS保留策略），因为APT注解处理器生成的类一般不需要注册反射，因此需要排除
     */
    private static boolean hasGeneratedAnnotation(Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            var found = new boolean[1];
            new ClassReader(in).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                    if (descriptor.endsWith("/Generated;")) {
                        found[0] = true;
                    }
                    return null;
                }
            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return found[0];
        }
        catch (IOException ex) {
            return false;
        }
    }

    private static boolean isJavaBean(Class<?> type) {
        if (type.isEnum() || type.isAnnotation() || Throwable.class.isAssignableFrom(type)) {
            return false;
        }
        try {
            for (PropertyDescriptor descriptor : BeanUtils.getPropertyDescriptors(type)) {
                if (!"class".equals(descriptor.getName())) {
                    return true;
                }
            }
        }
        catch (BeansException ex) {
            return false;
        }
        return false;
    }

    private class JavaBeansContribution implements BeanFactoryInitializationAotContribution {

        private final Set<Class<?>> candidates;

        JavaBeansContribution(Set<Class<?>> candidates) {
            this.candidates = candidates;
        }

        @Override
        public void applyTo(GenerationContext generationContext, BeanFactoryInitializationCode code) {
            ReflectionHints hints = generationContext.getRuntimeHints().reflection();
            for (var candidate : this.candidates) {
                registrar.registerReflectionHints(hints, candidate);
            }
        }
    }
}
