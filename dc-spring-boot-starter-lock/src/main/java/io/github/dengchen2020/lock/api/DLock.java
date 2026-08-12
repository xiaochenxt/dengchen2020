package io.github.dengchen2020.lock.api;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 锁的简单接口
 * @author xiaochen
 * @since 2024/7/3
 */
public interface DLock {

    String LOCK_GLOBAL_PREFIX = "dc:lock:";

    void tryLockAndRun(String key, Runnable runnable);

    void tryLockAndRun(String key, long waitTime, TimeUnit unit, Runnable runnable) throws InterruptedException;

    <T> T tryLockAndRun(String key, Callable<T> callable) throws Throwable;

    <T> T tryLockAndRun(String key, long waitTime, TimeUnit unit, Callable<T> callable) throws Throwable;

    void lockAndRun(String key, Runnable runnable);

    <T> T lockAndRun(String key, Callable<T> callable) throws Throwable;

}
