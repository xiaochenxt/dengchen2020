package io.github.dengchen2020.core.aot;

import io.github.dengchen2020.aot.utils.AotUtils;
import io.github.dengchen2020.core.jdbc.Page;
import io.github.dengchen2020.core.jdbc.PageParam;
import io.github.dengchen2020.core.jdbc.SimplePage;
import io.github.dengchen2020.core.jdbc.StatsPage;
import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import io.github.dengchen2020.core.security.principal.SimpleUserAuthentication;
import io.github.dengchen2020.core.validation.*;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

/**
 * @author xiaochen
 * @since 2025/5/23
 */
public class CoreRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = AotUtils.newInstance(hints, classLoader);
        aotUtils.registerReflection(Page.class,PageParam.class,SimplePage.class,StatsPage.class,AnonymousAuthentication.class,SimpleUserAuthentication.class);
        aotUtils.registerSerializable(AnonymousAuthentication.class, SimpleUserAuthentication.class);
        if (aotUtils.isPresent("jakarta.validation.ConstraintValidator")) {
            aotUtils.registerPattern(TypeReference.of("jakarta.validation.ConstraintValidator"),"ValidationMessages.properties","ValidationMessages*.properties");
            hints.reflection().registerTypes(TypeReference.listOf(AllowedValuesValidator.class,
                            JsonObjectOrArrayValidator.class, JsonObjectValidator.class, JsonArrayValidator.class,
                            NotEmptyAllowNullValidatorForString.class, NotEmptyValidatorForCollection.class, NotEmptyValidatorForCollectionString.class,
                            AllowedValuesArrayValidator.class, AllowedValuesCollectionValidator.class),
                    builder -> builder.onReachableType(TypeReference.of("jakarta.validation.ConstraintValidator"))
                            .withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS));
        }
        if (aotUtils.isPresent("org.apache.coyote.AbstractProtocol")) {
            aotUtils.registerReflectionIfPresent(builder -> builder.withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS),"org.apache.coyote.http11.Http11NioProtocol",
                    "org.apache.coyote.AbstractProtocol","org.apache.coyote.http11.AbstractHttp11Protocol");
        }
    }

}
