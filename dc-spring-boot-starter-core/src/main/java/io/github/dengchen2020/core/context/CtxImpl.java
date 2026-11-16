package io.github.dengchen2020.core.context;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * 上下文存取默认实现
 * @author xiaochen
 * @since 2026/1/20
 */
final class CtxImpl implements Ctx {

    private String id;
    private final Map<String, Object> data;

    public CtxImpl() {
        this(null, new HashMap<>());
    }

    public CtxImpl(@Nullable String id, @NonNull Map<String, Object> data) {
        this.id = id;
        this.data = data;
    }

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
    public Ctx set(String key, Object value) {
        data.put(key, value);
        return this;
    }

    @Override
    public Ctx del(String key) {
        data.remove(key);
        return this;
    }

    @Override
    public Object get(String key) {
        return data.get(key);
    }

    @Override
    public Object getOrDefault(String key, Object defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }

    @Override
    public void foreach(BiConsumer<? super String, ? super Object> action) {
        data.forEach(action);
    }

    @Override
    public String toString() {
        return "CtxImpl{" +
                "id='" + id() + '\'' +
                ", data=" + data +
                '}';
    }
}
