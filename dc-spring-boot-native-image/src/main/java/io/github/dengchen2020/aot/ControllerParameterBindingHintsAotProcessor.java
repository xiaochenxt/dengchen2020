package io.github.dengchen2020.aot;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Controller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ControllerParameterBindingHintsAotProcessor implements BeanFactoryInitializationAotProcessor {

    private final BindingReflectionHintsRegistrar registrar = new BindingReflectionHintsRegistrar();

    @Override
    public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
        var controllerTypes = new LinkedHashSet<Class<?>>();
        for (var beanName : beanFactory.getBeanNamesForAnnotation(Controller.class)) {
            var beanType = beanFactory.getType(beanName);
            if (beanType != null) {
                var userClass = ClassUtils.getUserClass(beanType);
                controllerTypes.add(userClass);
                Collections.addAll(controllerTypes, ClassUtils.getAllInterfacesForClass(userClass));
            }
        }
        return controllerTypes.isEmpty() ? null : new ControllerParameterContribution(controllerTypes);
    }

    private class ControllerParameterContribution implements BeanFactoryInitializationAotContribution {

        private final Set<Class<?>> controllerTypes;

        ControllerParameterContribution(Set<Class<?>> controllerTypes) {
            this.controllerTypes = controllerTypes;
        }

        @Override
        public void applyTo(GenerationContext generationContext, BeanFactoryInitializationCode code) {
            var hints = generationContext.getRuntimeHints().reflection();
            for (var controllerType : this.controllerTypes) {
                ReflectionUtils.doWithMethods(controllerType, method -> {
                    for (var parameter : method.getParameters()) {
                        var methodParameter = MethodParameter.forParameter(parameter);
                        registrar.registerReflectionHints(hints, methodParameter.getGenericParameterType());
                    }
                }, method -> !method.isSynthetic() && !Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers()) && !method.isBridge());
            }
        }
    }

}
