package io.github.dengchen2020.security.authentication.token;

import io.github.dengchen2020.core.security.principal.Authentication;

/**
 * 有状态Token
 * @author xiaochen
 * @since 2025/8/28
 */
public interface StateToken {

    /**
     * 移除token
     *
     * @param token token值
     */
    void removeToken(String token);

    /**
     * 下线指定用户
     *
     * @param userId 用户唯一标识，默认取认证信息对象的name值，详见：{@link Authentication#getName()}
     */
    void offline(String userId);

}
