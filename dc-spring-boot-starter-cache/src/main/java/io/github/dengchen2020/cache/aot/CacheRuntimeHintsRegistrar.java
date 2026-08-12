package io.github.dengchen2020.cache.aot;

import io.github.dengchen2020.aot.utils.AotUtils;
import io.github.dengchen2020.cache.caffeine.CacheSyncMessageListener;
import io.github.dengchen2020.cache.caffeine.CacheSyncParam;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * @author xiaochen
 * @since 2025/5/23
 */
public class CacheRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = new AotUtils(hints, classLoader);
        aotUtils.registerReflection(CacheSyncParam.class);
        if (aotUtils.isPresent("org.springframework.data.redis.listener.adapter.MessageListenerAdapter")) {
            aotUtils.registerReflection(new MemberCategory[]{MemberCategory.INVOKE_DECLARED_METHODS}, CacheSyncMessageListener.class);
        }
    }

}
