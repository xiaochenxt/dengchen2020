package io.github.dengchen2020.core.context;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 上下文存取默认实现
 * @author xiaochen
 * @since 2026/1/20
 */
public final class CtxImpl implements Ctx {

    private String id;
    @JsonProperty private final Map<String, Object> data;

    public CtxImpl() {
        this(null, new HashMap<>());
    }

    public CtxImpl(@Nullable String id, @NonNull Map<String, Object> data) {
        this.id = id;
        this.data = data;
    }

    @JsonProperty
    @NonNull
    @Override
    public String id() {
        if(id == null || id.isBlank()) id = UUID.randomUUID().toString();
        return id;
    }

    @Override
    public Object put(String key, Object value) {
        return data.put(key, value);
    }

    @Override
    public Object putIfAbsent(String key, Object value) {
        return data.putIfAbsent(key, value);
    }

    @Override
    public Object remove(String key) {
        return data.remove(key);
    }

    @Override
    public CtxImpl set(String key, Object value) {
        data.put(key, value);
        return this;
    }

    @Override
    public CtxImpl del(String key) {
        data.remove(key);
        return this;
    }

    @Override
    public Object get(String key) {
        return data.get(key);
    }

    @Override
    public Object get(String key, Object defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }

    @Override
    public String toString() {
        return "CtxImpl{" +
                "id='" + id() + '\'' +
                ", data=" + data +
                '}';
    }
}
