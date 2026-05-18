package io.github.dengchen2020.core.security.principal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.security.Principal;

/**
 * 认证信息
 * @author xiaochen
 * @since 2024/4/28
 */
public interface Authentication extends Principal {

    static Authentication create(String userId) {
        return new SimpleUserAuthentication(userId);
    }

    /**
     * @return 用户id，最好是纯数字，不能包含:，特殊字符均不能有
     */
    String userId();

    /**
     * 返回用户的唯一标识
     * @return 用户的唯一标识
     */
    @JsonIgnore
    @Override
    default String getName() {
        return userId();
    }

    /**
     * 设置属性
     * @param name 属性名
     * @param value 属性值
     */
    default void setAttribute(String name, Object value) {}

    /**
     * 获取属性
     * @Note: 注意类型转换，反序列化后可能导致Long变Integer类型
     * @param name 属性名
     * @return 属性值
     */
    default Object getAttribute(String name) {
        return null;
    }

}
