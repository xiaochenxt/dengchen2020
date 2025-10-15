package io.github.dengchen2020.cache.caffeine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * 缓存同步参数
 * @author xiaochen
 * @since 2022/12/14
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class CacheSyncParam implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    /**
     * 1-移除指定缓存 2-清除所有缓存
     */
    private final Integer type;

    /**
     * 缓存名
     */
    private final String[] cacheName;

    /**
     * 要操作的缓存key
     */
    private final Object key;

    public CacheSyncParam() {
        this.cacheName = null;
        this.key = null;
        this.type = 2;
    }

    public CacheSyncParam(String cacheName) {
        this.cacheName = new String[]{cacheName};
        this.key = null;
        this.type = 2;
    }

    public CacheSyncParam(String[] cacheName) {
        this.cacheName = cacheName;
        this.key = null;
        this.type = 2;
    }

    public CacheSyncParam(String cacheName, Object key) {
        this.cacheName = new String[]{cacheName};
        this.key = key;
        this.type = 1;
    }

    public CacheSyncParam(String[] cacheName, Object key) {
        this.cacheName = cacheName;
        this.key = key;
        this.type = 1;
    }

    public Integer getType() {
        return type;
    }

    public String[] getCacheName() {
        return cacheName;
    }

    public Object getKey() {
        return key;
    }

    @Override
    public String toString() {
        return "CacheSyncParam{" +
                "type=" + type +
                ", cacheName=" + Arrays.toString(cacheName) +
                ", key=" + key +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheSyncParam that = (CacheSyncParam) o;
        return Objects.equals(type, that.type) && Arrays.equals(cacheName, that.cacheName) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(type);
        result = 31 * result + Arrays.hashCode(cacheName);
        result = 31 * result + Objects.hashCode(key);
        return result;
    }
}
