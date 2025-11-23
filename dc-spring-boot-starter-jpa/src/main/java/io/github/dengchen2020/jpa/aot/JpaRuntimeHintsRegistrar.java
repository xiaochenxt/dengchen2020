package io.github.dengchen2020.jpa.aot;

import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import io.github.dengchen2020.aot.utils.AotUtils;
import io.github.dengchen2020.jpa.base.BaseJpaRepositoryExecutor;
import io.github.dengchen2020.jpa.base.QuerydslJpaRepositoryExecutor;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * @author xiaochen
 * @since 2025/5/23
 */
public class JpaRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = new AotUtils(hints, classLoader);
        aotUtils.registerReflection(new MemberCategory[]{MemberCategory.INVOKE_DECLARED_CONSTRUCTORS}, PathBuilder.class, JPAQuery.class);
        aotUtils.registerReflection(QuerydslJpaRepositoryExecutor.class, BaseJpaRepositoryExecutor.class);
    }

}
