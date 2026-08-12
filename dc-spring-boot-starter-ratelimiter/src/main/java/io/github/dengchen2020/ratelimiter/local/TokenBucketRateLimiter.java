package io.github.dengchen2020.ratelimiter.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 单机限流实现
 * <p>令牌桶算法实现，精准限流，避免突发流量</p>
 * @author xiaochen
 * @since 2025/9/5
 */
public class TokenBucketRateLimiter implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TokenBucketRateLimiter.class);

    private static class TokenBucket {
        final int maxTokens;          // 时间窗口内最大请求数
        final long tokenIntervalMs;   // 令牌间隔（毫秒）
        private final AtomicLong nextTokenTime;  // 下一个令牌可生成时间
        volatile long lastAccessTime; // 最后访问时间
        final long inactiveThresholdMs; // 过期阈值

        TokenBucket(int ratePerSecond, long tokenTotalMs) {
            this.maxTokens = ratePerSecond;
            this.inactiveThresholdMs = tokenTotalMs + 1000L;
            this.tokenIntervalMs = tokenTotalMs / ratePerSecond;
            long createdTime = System.currentTimeMillis();
            this.nextTokenTime = new AtomicLong(createdTime);
            this.lastAccessTime = createdTime;
        }

        void updateLastAccessTime(long time) {
            this.lastAccessTime = time;
        }

        boolean isInactive() {
            return System.currentTimeMillis() - this.lastAccessTime > inactiveThresholdMs;
        }

        long getNextTokenTime() {
            return nextTokenTime.get();
        }

        boolean tryTakeToken() {
            long now = System.currentTimeMillis();
            long currentNextTime = nextTokenTime.get();
            if (now >= currentNextTime) {
                long newNextTime = now + tokenIntervalMs;
                if (nextTokenTime.compareAndSet(currentNextTime, newNextTime)) {
                    updateLastAccessTime(now);
                    return true;
                }
            }
            return false;
        }
    }

    private final ConcurrentHashMap<String, TokenBucket> bucketMap = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition tokenAvailable = lock.newCondition();
    private final ScheduledExecutorService cleaner;
    private final int maxKeyCount;
    private volatile boolean isShutdown = false;

    private final long tokenTotalMs;

    public TokenBucketRateLimiter() {
        this(Duration.ofSeconds(1), 50000);
    }

    public TokenBucketRateLimiter(Duration tokenDuration) {
        this(tokenDuration, 50000);
    }

    public TokenBucketRateLimiter(Duration tokenDuration, int maxKeyCount) {
        this.cleaner = Executors.newSingleThreadScheduledExecutor(Thread.ofVirtual().name("bucket-rate-limiter-cleaner").factory());
        // 清理间隔（毫秒）
        final long cleanupPeriodMs = 3_000;
        this.cleaner.scheduleWithFixedDelay(this::cleanupInactiveBuckets,
                cleanupPeriodMs, cleanupPeriodMs, TimeUnit.MILLISECONDS);
        this.tokenTotalMs = tokenDuration.toMillis();
        if (maxKeyCount <= 0 || maxKeyCount > 100000) throw new IllegalArgumentException("maxKeyCount必须在(0, 100000]范围内，当前值：" + maxKeyCount);
        this.maxKeyCount = maxKeyCount;
    }

    /**
     * 获取或创建令牌桶，超过最大数量时清理最早的Key
     */
    private TokenBucket getOrCreateBucket(String key, int ratePerSecond) {
        return bucketMap.computeIfAbsent(key, k -> {
            // 检查并处理容量限制
            enforceCapacityLimit();
            TokenBucket newBucket = new TokenBucket(ratePerSecond, tokenTotalMs);
            if (log.isDebugEnabled()) log.debug("创建新的令牌桶: {}", key);
            return newBucket;
        });
    }

    /**
     * 强制执行容量限制
     */
    private void enforceCapacityLimit() {
        if (bucketMap.size() >= maxKeyCount) {
            // 获取所有entry并按插入时间排序
            var entries = new ArrayList<>(bucketMap.entrySet());
            entries.sort(Comparator.comparingLong(a -> a.getValue().lastAccessTime));
            // 移除最老的entries直到满足容量限制
            int removeCount = entries.size() - maxKeyCount + 1;
            for (int i = 0; i < removeCount && i < entries.size(); i++) {
                String keyToRemove = entries.get(i).getKey();
                bucketMap.remove(keyToRemove);
                if (log.isWarnEnabled()) log.warn("Key数量已达上限({})，清理最早的Key: {}", maxKeyCount, keyToRemove);
            }
        }
    }

    /**
     * 阻塞式获取令牌
     * @param key 限流标识
     * @param ratePerSecond 时间窗口内最大请求数
     */
    public void acquire(String key, int ratePerSecond) {
        if (ratePerSecond <= 0) throw new IllegalArgumentException("速率必须大于0");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key不能为空");
        // 获取或创建令牌桶
        TokenBucket bucket = getOrCreateBucket(key, ratePerSecond);
        long requestStartMs = System.currentTimeMillis();
        lock.lock();
        try {
            while (true) {
                // 尝试获取令牌
                if (bucket.tryTakeToken()) {
                    // 令牌获取成功，唤醒其他等待线程
                    tokenAvailable.signalAll();
                    if (log.isDebugEnabled()) {
                        long now = System.currentTimeMillis();
                        long waitMs = now - requestStartMs;
                        log.debug("[限流通过] Key: {}, 通过时间: {}, 等待时间: {}ms, 下一个令牌时间: {}",
                                key, now, waitMs, bucket.getNextTokenTime());
                    }
                    return;
                }
                // 未到令牌生成时间：使用条件变量精确等待
                long waitMs = bucket.getNextTokenTime() - System.currentTimeMillis();
                if (waitMs > 0) {
                    try {
                        tokenAvailable.await(waitMs, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 支持等待超时的令牌获取
     * @param key 限流标识
     * @param ratePerSecond 时间窗口内最大请求数
     * @param waitTime 等待超时时间
     * @return true=获取成功，false=获取失败或超时
     */
    public boolean tryAcquire(String key, int ratePerSecond, Duration waitTime) {
        if (waitTime == null || waitTime.isNegative() || waitTime.toMillis() <= 0) {
            return tryAcquire(key, ratePerSecond);
        }
        // 计算等待截止时间和休眠间隔
        long maxWaitMs = waitTime.toMillis();
        long deadline = System.currentTimeMillis() + maxWaitMs;
        long sleepMs = Math.min(10, maxWaitMs); // 最大休眠10ms
        // 轮询等待令牌
        while (System.currentTimeMillis() < deadline) {
            // 尝试获取令牌
            if (tryAcquire(key, ratePerSecond)) return true;
            // 计算剩余等待时间
            long remainingMs = deadline - System.currentTimeMillis();
            if (remainingMs <= 0) break; // 超时退出
            // 短暂休眠避免过度轮询
            try {
                Thread.sleep(Math.min(sleepMs, remainingMs));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false; // 超时返回失败
    }

    /**
     * 非阻塞式尝试获取令牌
     * @param key 限流标识
     * @param ratePerSecond 时间窗口内最大请求数
     * @return true=获取成功，false=获取失败
     */
    public boolean tryAcquire(String key, int ratePerSecond) {
        // 参数和状态检查
        if (!validateRequest(key, ratePerSecond)) return false;
        // 获取或创建令牌桶
        TokenBucket bucket = getOrCreateBucket(key, ratePerSecond);
        boolean success = bucket.tryTakeToken();
        if (success && log.isTraceEnabled()) {
            log.trace("[非阻塞限流通过] Key: {}, 下一个令牌时间: {}",
                    key, bucket.getNextTokenTime());
        } else if (!success && log.isTraceEnabled()) {
            log.trace("[非阻塞限流失败] Key: {}, 未到令牌时间（当前: {}, 下一个: {}）",
                    key, System.currentTimeMillis(), bucket.getNextTokenTime());
        }
        return success;
    }

    private boolean validateRequest(String key, int ratePerSecond) {
        if (isShutdown) {
            if (log.isWarnEnabled()) log.warn("限流器已关闭，Key[{}]获取令牌失败", key);
            return false;
        }
        if (ratePerSecond <= 0) throw new IllegalArgumentException("速率必须大于0");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key不能为空");
        return true;
    }

    /**
     * 清理逻辑：遍历所有令牌桶，删除过期未访问的桶
     */
    private void cleanupInactiveBuckets() {
        int cleanupCount = 0;
        Iterator<Map.Entry<String, TokenBucket>> iterator = bucketMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TokenBucket> entry = iterator.next();
            String key = entry.getKey();
            TokenBucket bucket = entry.getValue();
            // 判断是否过期
            if (bucket.isInactive()) {
                iterator.remove();
                cleanupCount++;
                if (log.isDebugEnabled()) log.debug("清理无效的令牌桶: {}", key);
            }
        }
        if (cleanupCount > 0 && log.isDebugEnabled()) log.debug("定时清理完成：清理过期Key{}个", cleanupCount);
    }

    /**
     * 关闭限流器：停止清理线程，清空所有令牌桶
     */
    @Override
    public void close() {
        if (isShutdown) return;
        isShutdown = true;
        cleaner.shutdown();
        bucketMap.clear();
        if (log.isDebugEnabled()) log.debug("限流器已关闭");
    }

    /**
     * 重置指定Key的令牌桶
     */
    public void reset(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key不能为空");
        bucketMap.remove(key);
        if (log.isDebugEnabled()) log.debug("重置令牌桶: {}", key);
    }

    /**
     * 更新指定Key的限流阈值
     */
    public void updateRate(String key, int newRatePerSecond) {
        if (key == null || key.isBlank() || newRatePerSecond <= 0) throw new IllegalArgumentException("无效参数：key不能为空，newRatePerSecond必须>0");
        TokenBucket oldBucket = bucketMap.get(key);
        if (oldBucket == null) {
            if (log.isWarnEnabled()) log.warn("Key[{}]不存在，无需更新", key);
            return;
        }
        TokenBucket newBucket = new TokenBucket(newRatePerSecond, tokenTotalMs);
        newBucket.lastAccessTime = oldBucket.lastAccessTime;
        // 保留当前令牌生成进度
        newBucket.nextTokenTime.set(oldBucket.getNextTokenTime());
        bucketMap.put(key, newBucket);
        if (log.isDebugEnabled()) log.debug("Key[{}]限流阈值更新：旧{} -> 新{}", key, oldBucket.maxTokens, newRatePerSecond);
    }

    /**
     * 获取当前活跃Key数量
     */
    public int activeKeyCount() {
        return bucketMap.size();
    }

    public record BucketStatus(int maxTokens, long lastAccessTime, long nextTokenTime, boolean isInactive) {}

    /**
     * 获取指定Key的当前限流配置与状态
     */
    public BucketStatus bucketStatus(String key) {
        if (key == null || key.isBlank()) return null;
        TokenBucket bucket = bucketMap.get(key);
        if (bucket == null) return null;
        return new BucketStatus(bucket.maxTokens, bucket.lastAccessTime, bucket.getNextTokenTime(), bucket.isInactive());
    }

    /**
     * 获取最大Key数量配置
     */
    public int maxKeyCount() {
        return maxKeyCount;
    }
}
