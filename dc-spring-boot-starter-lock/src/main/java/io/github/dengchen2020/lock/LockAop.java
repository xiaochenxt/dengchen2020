package io.github.dengchen2020.lock;

import io.github.dengchen2020.lock.annotation.Lock;
import io.github.dengchen2020.lock.exception.LockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
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

    public static final String LOCK_GLOBAL_PREFIX = "dc:lock:";

    private final RedissonClient redissonClient;

    public LockAop(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around(value = "@annotation(lock)")
    public Object handle(ProceedingJoinPoint joinPoint, Lock lock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RLock rLock;
        if (lock.value().isBlank()) {
            rLock = lock.name().isBlank() ? redissonClient.getLock(LOCK_GLOBAL_PREFIX + signature.toString()) : redissonClient.getLock(LOCK_GLOBAL_PREFIX + lock.name());
        } else {
            Object[] args = joinPoint.getArgs();
            EvaluationContext context = new MethodBasedEvaluationContext(args.length != 1 ? null : args[0] , signature.getMethod(), args, parameterNameDiscoverer);
            String lockKey = parser.parseExpression(lock.value()).getValue(context, String.class);
            rLock = lock.name().isBlank() ? redissonClient.getLock(LOCK_GLOBAL_PREFIX + lockKey) : redissonClient.getLock(LOCK_GLOBAL_PREFIX + lock.name() + ":" + lockKey);
        }
        try {
            if (rLock.tryLock(lock.waitTime(), lock.lockTime(), lock.timeUnit())) {
                return joinPoint.proceed();
            } else {
                throw new LockException(lock.errorMsg());
            }
        } finally {
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 500;
    }
}
