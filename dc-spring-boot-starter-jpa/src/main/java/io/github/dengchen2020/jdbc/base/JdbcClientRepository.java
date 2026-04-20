package io.github.dengchen2020.jdbc.base;

import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * 为存储库中使用{@link JdbcClient}提供便捷
 * @author xiaochen
 * @since 2026/3/4
 */
@NullMarked
interface JdbcClientRepository {

    /**
     * 操作{@link JdbcClient}
     */
    JdbcClient jdbcClient();

}
