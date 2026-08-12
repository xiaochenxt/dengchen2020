package io.github.dengchen2020.lock;

import io.github.dengchen2020.lock.annotation.Lock;
import io.github.dengchen2020.lock.api.RedissonLock;
import io.github.dengchen2020.lock.exception.LockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * 锁注解实现
 *
 * @author xiaochen
 * @since 2024/7/1
 */
@Aspect
public class LockAop implements Ordered {

    private final RedissonLock redissonLock;

    public LockAop(RedissonLock redissonLock) {
        this.redissonLock = redissonLock;
    }

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around(value = "@annotation(lock)")
    public Object handle(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
        var signature = (MethodSignature) joinPoint.getSignature();
        String key;
        if (lock.value().isBlank()) {
            key = lock.name().isBlank() ? signature.toString() : lock.name();
        } else {
            var args = joinPoint.getArgs();
            var context = new MethodBasedEvaluationContext(args.length != 1 ? null : args[0] , signature.getMethod(), args, parameterNameDiscoverer);
            var value = parser.parseExpression(lock.value()).getValue(context, String.class);
            key = lock.name().isBlank() ? signature + ":" + value : lock.name() + ":" + value;
        }
        var rLock = redissonLock.getLock(key);
        try {
            if (rLock.tryLock(lock.waitTime(), lock.lockTime(), lock.timeUnit())) {
                return joinPoint.proceed();
            } else {
                throw new LockException(lock.errorMsg());
            }
        } finally {
            redissonLock.unlock(rLock);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 500;
    }
}
