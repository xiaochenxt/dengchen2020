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

    public CacheSyncParam() {
        this(2, null, null);
    }

    public CacheSyncParam(String cacheName) {
        this(2, new String[]{cacheName}, null);
    }

    public CacheSyncParam(String[] cacheName) {
        this(2, cacheName, null);
    }

    public CacheSyncParam(String cacheName, Object key) {
        this(1, new String[]{cacheName}, key);
    }

    public CacheSyncParam(String[] cacheName, Object key) {
        this(1, cacheName, key);
    }

}
