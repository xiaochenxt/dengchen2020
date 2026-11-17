package io.github.dengchen2020.aot;

import io.github.dengchen2020.aot.utils.AotUtils;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static io.github.dengchen2020.aot.utils.CollectUtils.EMPTY_STRING_ARRAY;

/**
 * 将springboot项目中不含第三方库的所有类注册反射调用，为所有实现了Serializable的注册序列化，可解决90%的运行时错误问题
 * @author xiaochen
 * @since 2025/5/23
 */
class AllRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    @SuppressWarnings("unchecked")
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        try {
            AotUtils aotUtils = new AotUtils(hints, classLoader);
            List<String> list = new ArrayList<>();
            for (var c : aotUtils.findSpringBootApplicationClasses()) {
                String packageName = c.getPackageName();
                list.add(packageName);
            }
            aotUtils.registerPattern("*.properties");
            var classes = aotUtils.collectClass(list.toArray(EMPTY_STRING_ARRAY));
            classes.forEach(c -> aotUtils.registerSerializable(TypeReference.of(c), c));
            aotUtils.registerPattern(aotUtils.findResources("",
                    name -> !name.startsWith("META-INF/native-image/"))
                    .toArray(EMPTY_STRING_ARRAY)); // 注册用户所有的资源文件
        } catch (IOException ignored) {}
    }

}
