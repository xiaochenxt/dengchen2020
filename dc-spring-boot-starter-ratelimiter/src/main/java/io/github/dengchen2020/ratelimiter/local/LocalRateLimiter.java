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
import java.util.concurrent.atomic.AtomicLong;

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
        long currentTime = System.currentTimeMillis();
        var counter = counters.computeIfAbsent(limitKey, _ -> new WindowCounter(currentTime));
        return counter.checkAndIncrement(limitNum, currentTime, windowMillis);
    }

    /**
     * 清理过期的key
     */
    private void cleanExpiredKeys() {
        if (counters.isEmpty()) return;
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime - windowMillis;
        counters.entrySet().removeIf(entry -> entry.getValue().windowStartTime.get() < expireTime);
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
        // 窗口开始时间
        private final AtomicLong windowStartTime;
        // 窗口计数
        private final AtomicInteger count = new AtomicInteger(0);

        WindowCounter(long windowStartTime) {
            this.windowStartTime = new AtomicLong(windowStartTime);
        }

        /**
         * 检查并计数
         */
        boolean checkAndIncrement(int limitNum, long currentTime, long windowMillis) {
            // 检查窗口是否需要重置
            long currentWindowStart = windowStartTime.get();
            if (currentTime - currentWindowStart >= windowMillis) {
                // 窗口重置
                if (windowStartTime.compareAndSet(currentWindowStart, currentTime)) {
                    count.set(1);
                    return false; // 第一次总是允许
                }
            }
            // 窗口有效，尝试增加计数
            int currentCount;
            do {
                currentCount = count.get();
                if (currentCount >= limitNum) return true; // 超过限制
            } while (!count.compareAndSet(currentCount, currentCount + 1));
            return false;
        }
    }
}
