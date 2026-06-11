package io.github.dengchen2020.cache.caffeine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serial;
import java.io.Serializable;

/**
 * 缓存同步参数
 *
 * @param type      1-移除指定缓存 2-清除所有缓存
 * @param cacheName 缓存名
 * @param key       要操作的缓存key
 * @author xiaochen
 * @since 2022/12/14
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public record CacheSyncParam(int type, String[] cacheName, Object key) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 移除指定缓存
     */
    static final int TYPE_EVICT = 1;
    /**
     * 清除所有缓存
     */
    static final int TYPE_CLEAR = 2;

    /**
     * 构建清除所有缓存的参数
     */
    public static CacheSyncParam clearAll() {
        return new CacheSyncParam(TYPE_CLEAR, null, null);
    }

    /**
     * 构建清除指定{@code cacheName}的缓存的参数
     * @param cacheName 缓存名
     */
    public static CacheSyncParam clear(String[] cacheName) {
        return new CacheSyncParam(TYPE_CLEAR, cacheName, null);
    }

    /**
     * 构建移除指定缓存的参数
     * @param cacheName 缓存名
     * @param key       要清除的缓存key
     */
    public static CacheSyncParam evict(String[] cacheName, Object key) {
        return new CacheSyncParam(TYPE_EVICT, cacheName, key);
    }

}
