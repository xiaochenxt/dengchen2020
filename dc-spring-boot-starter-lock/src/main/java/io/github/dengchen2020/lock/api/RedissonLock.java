package io.github.dengchen2020.lock.api;

import io.github.dengchen2020.lock.exception.LockException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * redisson分布式锁使用简化实现
 *
 * @author xiaochen
 * @since 2024/7/1
 */
public class RedissonLock implements DLock, DisposableBean {

    private final RedissonClient redissonClient;

    public RedissonLock(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void destroy() {
        redissonClient.shutdown();
    }

    /**
     * 获取{@link RLock}实例（非公平锁）
     * @param key 锁key
     * @return {@link RLock}
     */
    public RLock getLock(String key) {
        return redissonClient.getLock(LOCK_GLOBAL_PREFIX + key);
    }

    /**
     * 解锁
     * @param rLock {@link RLock}实例
     */
    public void unlock(RLock rLock) {
        if (rLock.isLocked() && rLock.isHeldByCurrentThread()) {
            rLock.unlock();
        }
    }

    protected LockException defaultException() {
        throw new LockException("请求人数过多，请稍后再试");
    }

    @Override
    public <T> T tryLockAndRun(String key, Callable<T> callable) throws Throwable {
        var rLock = getLock(key);
        try {
            if (rLock.tryLock()) {
                return callable.call();
            } else {
                throw defaultException();
            }
        } finally {
            unlock(rLock);
        }
    }

    @Override
    public void tryLockAndRun(String key, Runnable runnable) {
        var rLock = getLock(key);
        try {
            if (rLock.tryLock()) {
                runnable.run();
            } else {
                throw defaultException();
            }
        } finally {
            unlock(rLock);
        }
    }

    public void tryLockAndRun(String key, Runnable runnable, Runnable failRunnable) {
        var rLock = getLock(key);
        try {
            if (rLock.tryLock()) {
                runnable.run();
            } else {
                if (failRunnable != null) failRunnable.run();
            }
        } finally {
            unlock(rLock);
        }
    }

    @Override
    public void tryLockAndRun(String key, long waitTime, TimeUnit unit, Runnable runnable) throws InterruptedException {
        tryLockAndRun(key, waitTime, -1, unit, runnable);
    }

    @Override
    public <T> T tryLockAndRun(String key, long waitTime, TimeUnit unit, Callable<T> callable) throws Throwable {
        return tryLockAndRun(key, waitTime, -1, unit, callable);
    }

    public <T> T tryLockAndRun(String key, long waitTime, long leaseTime, TimeUnit unit, Callable<T> callable) throws Throwable {
        var rLock = getLock(key);
        try {
            if (rLock.tryLock(waitTime, leaseTime, unit)) {
                return callable.call();
            } else {
                throw defaultException();
            }
        } finally {
            unlock(rLock);
        }
    }

    public void tryLockAndRun(String key, long waitTime, long leaseTime, TimeUnit unit, Runnable runnable) throws InterruptedException {
        var rLock = getLock(key);
        try {
            if (rLock.tryLock(waitTime, leaseTime, unit)) {
                runnable.run();
            } else {
                throw defaultException();
            }
        } finally {
            unlock(rLock);
        }
    }

    @Override
    public void lockAndRun(String key, Runnable runnable) {
        var rLock = getLock(key);
        try {
            rLock.lock();
            runnable.run();
        } finally {
            unlock(rLock);
        }
    }

    @Override
    public <T> T lockAndRun(String key, Callable<T> callable) throws Throwable {
        var rLock = getLock(key);
        try {
            rLock.lock();
            return callable.call();
        } finally {
            unlock(rLock);
        }
    }
}
