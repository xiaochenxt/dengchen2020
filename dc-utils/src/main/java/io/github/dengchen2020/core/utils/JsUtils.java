package io.github.dengchen2020.core.utils;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.jspecify.annotations.NonNull;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.Map;

/**
 * JavaScript工具类
 * <p>警告：当前版本不完全支持虚拟线程（未来版本会支持），因此在native编译时需配置-Dtruffle.UseFallbackRuntime=true</p>
 * <p>使用时需引入依赖：</p>
 * <pre>
 * {@code
 * <dependency>
 *   <groupId>org.graalvm.polyglot</groupId>
 *   <artifactId>js</artifactId>
 *   <version>25.0.0</version>
 *   <type>pom</type>
 * </dependency>
 *   <dependency>
 *   <groupId>org.graalvm.js</groupId>
 *   <artifactId>js-scriptengine</artifactId>
 *   <version>25.0.0</version>
 * </dependency>
 * }
 * </pre>
 *
 * @author xiaochen
 * @since 2025/9/11
 */
public abstract class JsUtils {

    // 线程局部变量存储脚本引擎
    private static final ThreadLocal<GraalJSScriptEngine> engineLocal = ThreadLocal.withInitial(() -> {
        // 配置GraalJS引擎
        return GraalJSScriptEngine.create(null, Context.newBuilder("js")
                .allowHostAccess(HostAccess.newBuilder() //配置java主机访问策略
                .allowAccessAnnotatedBy(HostAccess.Export.class) // 使用@HostAccess.Export导出可访问的构造函数、字段、方法
                .methodScoping(true) //方法作用域隔离
                .allowArrayAccess(true) // 允许对Java Array的访问，推荐开启
                .allowListAccess(true) // 允许对Java List、Iterable、Iterator的访问，推荐开启
                .allowMapAccess(true) // 允许对Java Map的访问，推荐开启
                        .build()));
    });

    /**
     * 执行JS表达式
     */
    public static Object eval(@NonNull String expr) {
        try {
            return engineLocal.get().eval(expr);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("js代码执行发生异常", e);
        }
    }

    /**
     * 执行带变量的JS代码
     */
    public static Object eval(@NonNull String script, Map<String, Object> vars) {
        ScriptEngine engine = engineLocal.get();
        Bindings bindings = engine.createBindings();
        if (vars != null) bindings.putAll(vars);
        try {
            return engine.eval(script, bindings);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("js代码执行发生异常", e);
        }
    }

    /**
     * 调用JS函数
     */
    public static Object call(@NonNull String funcScript,@NonNull String funcName, Object... params) {
        ScriptEngine engine = engineLocal.get();
        try {
            engine.eval(funcScript);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("js代码定义函数发生异常", e);
        }
        try {
            return ((Invocable) engine).invokeFunction(funcName, params);
        } catch (ScriptException e) {
            throw new IllegalArgumentException("js代码执行发生异常", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("js代码执行不存在的函数", e);
        }
    }

    /**
     * 清除当前线程的引擎实例
     */
    public static void clear() {
        engineLocal.get().close();
        engineLocal.remove();
    }

}
