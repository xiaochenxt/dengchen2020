package io.github.dengchen2020.jdbc.base;

import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.function.Function;

/**
 * 为存储库中使用{@link JdbcClient}提供便捷
 * @author xiaochen
 * @since 2026/3/4
 */
@NullMarked
interface JdbcClientRepository {

    /**
     * 操作{@link JdbcClient}并返回执行结果
     */
    <R> R execute(Function<JdbcClient, R> function);

}
