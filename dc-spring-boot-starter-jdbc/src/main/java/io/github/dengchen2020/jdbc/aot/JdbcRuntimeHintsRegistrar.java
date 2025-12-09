package io.github.dengchen2020.jdbc.aot;

import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.sql.SQLQuery;
import io.github.dengchen2020.aot.utils.AotUtils;
import io.github.dengchen2020.jdbc.base.BaseJdbcRepositoryExecutor;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * JDBC的aot处理
 * @author xiaochen
 * @since 2025/5/23
 */
public class JdbcRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = new AotUtils(hints, classLoader);
        aotUtils.registerReflection(new MemberCategory[]{MemberCategory.INVOKE_DECLARED_CONSTRUCTORS}, PathBuilder.class, SQLQuery.class);
        aotUtils.registerReflection(BaseJdbcRepositoryExecutor.class);
    }

}
