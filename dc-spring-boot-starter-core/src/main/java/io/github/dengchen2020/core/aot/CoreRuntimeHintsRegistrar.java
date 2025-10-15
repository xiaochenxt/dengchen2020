package io.github.dengchen2020.core.aot;

import io.github.dengchen2020.core.validation.*;
import io.github.dengchen2020.aot.utils.AotUtils;
import io.github.dengchen2020.core.event.ScheduledHandleBeforeEvent;
import io.github.dengchen2020.core.jdbc.PageParam;
import io.github.dengchen2020.core.jdbc.SimplePage;
import io.github.dengchen2020.core.jdbc.StatsPage;
import io.github.dengchen2020.core.security.principal.AnonymousAuthentication;
import io.github.dengchen2020.core.security.principal.Authentication;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * @author xiaochen
 * @since 2025/5/23
 */
public class CoreRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = AotUtils.newInstance(hints, classLoader);
        aotUtils.registerReflection(new MemberCategory[]{MemberCategory.INVOKE_DECLARED_CONSTRUCTORS}, AllowedValuesValidator.class, JsonValidator.class, NotEmptyAllowNullValidatorForString.class, NotEmptyValidatorForCollection.class, NotEmptyValidatorForCollectionString.class,
                PageParam.class, SimplePage.class, StatsPage.class, ScheduledHandleBeforeEvent.class);
        aotUtils.registerSerializable(Authentication.class, AnonymousAuthentication.class);
    }

}
