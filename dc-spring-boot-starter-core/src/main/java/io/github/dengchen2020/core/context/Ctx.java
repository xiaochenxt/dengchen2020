package io.github.dengchen2020.core.context;

import org.jspecify.annotations.NonNull;

/**
 * 上下文存取
 * @author xiaochen
 * @since 2026/1/20
 */
public interface Ctx {

    static Ctx create() {
        return new CtxImpl();
    }

    @NonNull
    String id();

    Object put(String key, Object value);

    Object putIfAbsent(String key, Object value);

    Object remove(String key);

    Ctx set(String key, Object value);

    Ctx del(String key);

    Object get(String key);

    Object get(String key, Object defaultValue);

}
