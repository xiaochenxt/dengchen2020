package io.github.dengchen2020.lock;

import io.github.dengchen2020.lock.annotation.Lock;
import io.github.dengchen2020.lock.api.RedissonLock;
import io.github.dengchen2020.lock.exception.LockException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ConstructorResolver;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.MethodResolver;
import org.springframework.expression.TypeLocator;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 锁注解实现
 *
 * @author xiaochen
 * @since 2024/7/1
 */
public class LockAop implements MethodInterceptor, Ordered {

    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 500;

    private final RedissonLock redissonLock;

    public LockAop(RedissonLock redissonLock) {
        this.redissonLock = redissonLock;
    }

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 禁止锁表达式使用 T(...) 类型引用
     */
    private static final TypeLocator LOCK_TYPE_LOCATOR = typeName -> {
        throw new EvaluationException("锁表达式不允许类型引用：" + typeName);
    };
    /**
     * 禁止锁表达式使用 new 构造对象
     */
    private static final List<ConstructorResolver> NO_CONSTRUCTOR_RESOLVERS = Collections.emptyList();
    /**
     * 禁止锁表达式中的显式方法调用（属性访问不受影响）
     */
    private static final List<MethodResolver> NO_METHOD_RESOLVERS = Collections.emptyList();

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        var method = invocation.getMethod();
        var lock = method.getAnnotation(Lock.class);
        String key;
        if (lock.value().isBlank()) {
            key = lock.name().isBlank() ? methodName(method) : lock.name();
        } else {
            var args = invocation.getArguments();
            var context = new MethodBasedEvaluationContext(args.length != 1 ? null : args[0], method, args, parameterNameDiscoverer);
            context.setTypeLocator(LOCK_TYPE_LOCATOR);
            context.setConstructorResolvers(NO_CONSTRUCTOR_RESOLVERS);
            context.setMethodResolvers(NO_METHOD_RESOLVERS);
            var value = parser.parseExpression(lock.value()).getValue(context, String.class);
            key = lock.name().isBlank() ? methodName(method) + ":" + value : lock.name() + ":" + value;
        }
        var rLock = redissonLock.getLock(key);
        try {
            if (rLock.tryLock(lock.waitTime(), lock.lockTime(), lock.timeUnit())) {
                return invocation.proceed();
            } else {
                throw new LockException(lock.errorMsg());
            }
        } finally {
            redissonLock.unlock(rLock);
        }
    }

    /**
     * 锁 key 格式：com.dc.Lock.method(String,int)
     */
    private static String methodName(Method method) {
        var sb = new StringBuilder();
        sb.append(method.getDeclaringClass().getName()).append('.')
                .append(method.getName()).append('(');
        var types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(types[i].getSimpleName());
        }
        return sb.append(')').toString();
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
