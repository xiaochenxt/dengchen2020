package io.github.dengchen2020.aot;

import io.github.dengchen2020.aot.utils.AotUtils;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基本注册
 * @author xiaochen
 * @since 2025/5/23
 */
class DcRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = new AotUtils(hints, classLoader);
        staticResource(aotUtils);
        aotUtils.registerReflection(LinkedHashMap.class, HashMap.class, ArrayList.class, List.class, Map.class, Set.class, ConcurrentMap.class, ConcurrentHashMap.class, HashSet.class);
    }

    /**
     * 静态资源注册
     * @param aotUtils
     */
    private void staticResource(AotUtils aotUtils) {
        aotUtils.registerPattern("static/*");
    }

}
