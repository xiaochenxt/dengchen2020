package io.github.dengchen2020.aot;

import io.github.dengchen2020.aot.utils.FeatureUtils;
import org.graalvm.nativeimage.hosted.Feature;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

/**
 * GraalVM Feature ，它将与某些模式匹配的布尔字段值替换为提前预先计算的值，而不会导致类构建时初始化。 </br>
 * 有可能通过 </br>
 * -Ddc.native.precompute.log=verbose </br>
 * 作为 </br>
 * native-image </br>
 * compiler build 参数来显示有关预计算字段的详细日志 </br>
 * @author xiaochen
 * @since 2025/10/29
 */
class PreComputeFieldFeature implements Feature {

    private static final boolean verbose =
            "verbose".equalsIgnoreCase(System.getProperty("dc.native.precompute.log"));

    private static final Pattern[] patterns = {
            Pattern.compile(Pattern.quote("io.github.dengchen2020.") + ".*#.*Present"),
    };


    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        access.registerSubtypeReachabilityHandler(this::iterateFields, Object.class);
    }

    // 为可访问的每种类型调用此方法。
    private void iterateFields(DuringAnalysisAccess access, Class<?> subtype) {
        try {
            FeatureUtils featureUtils = new FeatureUtils(access.getApplicationClassLoader());
            for (Field field : subtype.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (!Modifier.isStatic(modifiers) || !Modifier.isFinal(modifiers) || field.isEnumConstant() ||
                        (field.getType() != boolean.class && field.getType() != Boolean.class)) {
                    continue;
                }
                String fieldIdentifier = field.getDeclaringClass().getName() + "#" + field.getName();
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(fieldIdentifier).matches()) {
                        try {
                            Object fieldValue = provideFieldValue(field, featureUtils);
                            access.registerFieldValueTransformer(field, (receiver, originalValue) -> fieldValue);
                            if (verbose) {
                                System.out.println(
                                        "Field " + fieldIdentifier + " set to " + fieldValue + " at build time");
                            }
                        }
                        catch (Throwable ex) {
                            if (verbose) {
                                System.out.println("Field " + fieldIdentifier + " will be evaluated at runtime " +
                                        "due to this error during build time evaluation: " + ex);
                            }
                        }
                    }
                }
            }
        }
        catch (NoClassDefFoundError ex) {
            // 跳过类路径中未具有所有字段类型的类
        }
    }

    // 当字段值写入图像堆或字段常量折叠时，将调用此方法。
    private Object provideFieldValue(Field field, FeatureUtils featureUtils)
            throws NoSuchFieldException, IllegalAccessException {

        Class<?> throwawayClass = featureUtils.loadClass(field.getDeclaringClass().getName());
        Field throwawayField = throwawayClass.getDeclaredField(field.getName());
        throwawayField.setAccessible(true);
        return throwawayField.get(null);
    }

    @Override
    public String getDescription() {
        return "它将与某些模式匹配的布尔字段值替换为提前预先计算的值，而不会导致类构建时初始化";
    }
}
