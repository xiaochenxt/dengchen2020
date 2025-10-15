package io.github.dengchen2020.ratelimiter;

/**
 * 限流接口
 *
 * @author xiaochen
 * @since 2024/4/18
 */
public interface RateLimiter extends AutoCloseable {

    /**
     * 是否被限制
     *
     * @param limitKey 限制标识符
     * @param limitNum 限制的次数
     * @return true：被限制 false：未被限制
     */
    boolean limit(String limitKey, int limitNum);

    @Override
    default void close() {}
}
