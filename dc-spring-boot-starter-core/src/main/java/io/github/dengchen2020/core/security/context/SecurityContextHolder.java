package io.github.dengchen2020.core.security.context;

import io.github.dengchen2020.core.security.principal.Authentication;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证信息上下文管理
 * @author xiaochen
 * @since 2024/4/25
 */
public class SecurityContextHolder {

    private final static ThreadLocal<Authentication> currentAuthentication = new ThreadLocal<>();

    private final static ThreadLocal<Map<String, Object>> resources = new ThreadLocal<>();

    /**
     * 保存认证信息
     * @param authentication 认证信息
     */
    public static void setAuthentication(Authentication authentication) {
        if (authentication == null) {
            currentAuthentication.remove();
        }else {
            currentAuthentication.set(authentication);
        }
    }

    /**
     * 获取认证信息
     * @return 认证信息
     */
    public static Authentication getAuthentication() {
        return currentAuthentication.get();
    }

    /**
     * 清除认证信息上下文
     */
    public static void clear() {
        currentAuthentication.remove();
        resources.remove();
    }

    /**
     * 获取资源
     * @param key 资源key
     * @return 资源
     */
    @Nullable
    public static Object getResource(String key) {
        if (key == null) return null;
        Map<String, Object> map = resources.get();
        if (map == null) return null;
        return map.get(key);
    }

    /**
     * 获取资源
     * @param key 资源key
     * @param defaultValue 资源不存在时返回默认值
     * @return 资源
     */
    @Nullable
    public static Object getResource(String key, Object defaultValue) {
        if (key == null) return defaultValue;
        Map<String, Object> map = resources.get();
        if (map == null) return defaultValue;
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * 绑定资源
     * @param key 资源key
     * @param value 资源
     */
    public static void bindResource(@NonNull String key, Object value) {
        Map<String, Object> map = resources.get();
        if (map == null) {
            map = new HashMap<>();
            resources.set(map);
        }
        map.put(key, value);
    }

    /**
     * 解绑资源
     * @param key 资源key
     */
    public static void unbindResource(@NonNull String key) {
        Map<String, Object> map = resources.get();
        if (map == null) return;
        map.remove(key);
    }

}
