package io.github.dengchen2020.ratelimiter.local;

import io.github.dengchen2020.ratelimiter.RateLimiter;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 单机限流实现
 *
 * @author xiaochen
 * @since 2024/4/18
 */
@NullMarked
public class LocalRateLimiter implements RateLimiter {

    // 时间窗口长度(毫秒)
    private final long windowMillis;

    // 存储每个key的计数信息
    private final Map<String, WindowCounter> counters;

    // 定时清理线程池
    private final ScheduledExecutorService cleaner;

    /**
     * 限流实例化
     *
     * @param duration 时间窗口
     */
    public LocalRateLimiter(Duration duration) {
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("无效的时间窗口: " + duration);
        }
        this.windowMillis = duration.toMillis();
        // 清理间隔
        long cleanInterval = 3000L;
        this.counters = new ConcurrentHashMap<>();
        this.cleaner = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("rate-limiter-cleaner").factory());
        this.cleaner.scheduleWithFixedDelay(
                this::cleanExpiredKeys,
                2000L,
                cleanInterval,
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * 限流判断
     *
     * @param limitKey 限流标识符（如用户ID、接口路径）
     * @param limitNum 时间窗口内的最大允许次数
     * @return true：触发限流，false：允许请求
     */
    @Override
    public boolean limit(String limitKey, int limitNum) {
        if (limitNum <= 0) return true;
        WindowCounter counter = counters.compute(limitKey, (k, v) -> {
            if (v == null) return new WindowCounter(System.currentTimeMillis());
            long currentTime = System.currentTimeMillis();
            if (currentTime - v.windowStartTime >= windowMillis) v.reset(currentTime);
            return v;
        });
        return counter.checkAndIncrement(limitNum);
    }

    /**
     * 清理过期的key
     */
    private void cleanExpiredKeys() {
        if (counters.isEmpty()) return;
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime - windowMillis;
        counters.entrySet().removeIf(entry -> entry.getValue().windowStartTime < expireTime);
    }

    @Override
    public void close() {
        // 关闭定时任务
        cleaner.shutdown();
        try {
            if (!cleaner.awaitTermination(30, TimeUnit.SECONDS)) {
                cleaner.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleaner.shutdownNow();
        }
        // 清理资源
        counters.clear();
    }

    /**
     * 窗口计数器，用于跟踪每个key在当前窗口的请求次数
     */
    private static final class WindowCounter {
        // 窗口开始时间，使用volatile保证可见性
        volatile long windowStartTime;
        // 请求计数
        private final AtomicInteger count = new AtomicInteger(0);

        WindowCounter(long windowStartTime) {
            this.windowStartTime = windowStartTime;
        }

        /**
         * 重置计数器到新的窗口
         */
        void reset(long newWindowStartTime) {
            this.windowStartTime = newWindowStartTime;
            count.set(0);
        }

        /**
         * 先检查是否超过限制，再决定是否增加计数
         * 这样可以确保计数与实际允许的请求数一致
         * @return true: 超过限制，false: 未超过限制
         */
        boolean checkAndIncrement(int limitNum) {
            int current;
            do {
                current = count.get();
                // 如果当前计数已超过限制，直接返回true
                if (current >= limitNum) return true;
                // 否则尝试将计数加1
            } while (!count.compareAndSet(current, current + 1));
            // 成功增加计数且未超过限制
            return false;
        }
    }
}
