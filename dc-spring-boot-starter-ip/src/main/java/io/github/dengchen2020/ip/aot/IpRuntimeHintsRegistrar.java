package io.github.dengchen2020.ip.aot;

import io.github.dengchen2020.aot.utils.AotUtils;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * @author xiaochen
 * @since 2025/6/20
 */
public class IpRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        AotUtils aotUtils = new AotUtils(hints, classLoader);
        aotUtils.registerResourcesIfPresent("ip.xdb", "ipv6.xdb", "ip.dat");
    }

}
