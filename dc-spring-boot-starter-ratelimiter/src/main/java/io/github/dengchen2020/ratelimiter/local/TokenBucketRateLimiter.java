package io.github.dengchen2020.ratelimiter.local;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 单机限流实现
 * <p>令牌桶算法实现，精准限流，避免突发流量</p>
 * @author xiaochen
 * @since 2025/9/5
 */
public class TokenBucketRateLimiter implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TokenBucketRateLimiter.class);

    /**
     * 定时清理线程的检查周期（3秒，小于清理阈值，确保及时清理）
     */
    private static final long CLEANUP_PERIOD_MS = 3 * 1000;

    private static class TokenBucket {
        final int maxTokens;          // 时间窗口内最大请求数
        final long tokenIntervalMs;   // 令牌间隔（毫秒）
        volatile long nextTokenTime;  // 下一个令牌可生成时间（毫秒，volatile确保可见性）
        volatile long lastAccessTime; // 最后一次请求访问时间（毫秒，用于判断是否 inactive）
        final ReentrantLock lock;     // 独占锁，确保令牌生成单线程
        final long inactiveThresholdMs; // 非活动阈值（毫秒），超过说明过期则清理

        TokenBucket(int ratePerSecond) {
            this(ratePerSecond, 1000L);
        }

        TokenBucket(int ratePerSecond, long tokenTotalMs) {
            this.maxTokens = ratePerSecond;
            this.inactiveThresholdMs = tokenTotalMs + 1000L; // 过期阈值=时间窗口+1秒，留缓冲
            this.tokenIntervalMs = tokenTotalMs / ratePerSecond; // 令牌间隔=总时间/最大请求数
            long createdTime = System.currentTimeMillis();
            // 首次令牌时间设为当前时间，允许立即获取第一个令牌
            this.nextTokenTime = createdTime;
            // 初始：最后访问时间设为创建时间
            this.lastAccessTime = createdTime;
            this.lock = new ReentrantLock();
        }

        /**
         * 更新最后访问时间（每次请求通过时调用）
         */
        void updateLastAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        /**
         * 更新最后访问时间（每次请求通过时调用）
         */
        void updateLastAccessTime(long lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
        }

        /**
         * 判断当前令牌桶是否已过期
         * @return true=过期，false=活跃
         */
        boolean isInactive() {
            return System.currentTimeMillis() - this.lastAccessTime > inactiveThresholdMs;
        }

        /**
         * 非阻塞尝试获取令牌（供tryAcquire调用）
         * @return true=获取成功，false=获取失败（未到令牌时间）
         */
        boolean tryTakeToken() {
            long nowMs = System.currentTimeMillis();
            // 1. 检查是否到令牌生成时间
            if (nowMs >= nextTokenTime) {
                // 2. 更新下一个令牌时间（避免时间漂移）
                nextTokenTime = nowMs + tokenIntervalMs;
                // 3. 标记Key为活跃
                updateLastAccessTime(nowMs);
                return true;
            }
            // 未到令牌时间，返回失败
            return false;
        }
    }

    private final Map<String, TokenBucket> bucketMap = new ConcurrentHashMap<>();
    // 记录Key的插入顺序，用于超过限制时清理最早的Key
    private final Map<String, TokenBucket> keyOrderMap = new LinkedHashMap<>();
    // 控制Key数量和顺序Map的锁
    private final ReentrantLock keyLock = new ReentrantLock();
    // 最大Key数量限制
    private final int maxKeyCount;

    // 定时清理线程
    private final Thread cleanupThread;
    // 标记限流器是否已关闭（用于停止清理线程）
    private volatile boolean isShutdown = false;

    private final long tokenTotalMs;

    public TokenBucketRateLimiter() {
        this(Duration.ofSeconds(1), 50000);
    }

    /**
     * @param tokenDuration 令牌生成的时间窗口
     */
    public TokenBucketRateLimiter(Duration tokenDuration) {
        this(tokenDuration, 50000);
    }

    /**
     * @param tokenDuration 令牌生成的时间窗口
     * @param maxKeyCount 最大允许的Key数量
     */
    public TokenBucketRateLimiter(Duration tokenDuration, int maxKeyCount) {
        this.cleanupThread = Thread.ofVirtual().name("rate-limiter-cleanup-thread").start(this::cleanupInactiveBuckets);
        this.tokenTotalMs = tokenDuration.toMillis();
        if (maxKeyCount <= 0 || maxKeyCount > 100000) throw new IllegalArgumentException("maxKeyCount必须在(0, 100000]范围内，当前值：" + maxKeyCount);
        this.maxKeyCount = maxKeyCount;
    }

    /**
     * 获取或创建令牌桶，超过最大数量时清理最早的Key
     */
    private TokenBucket getOrCreateBucket(String key, int ratePerSecond) {
        // 先检查是否已存在，避免加锁开销
        TokenBucket existingBucket = bucketMap.get(key);
        if (existingBucket != null) return existingBucket;
        // 加锁处理新Key创建
        keyLock.lock();
        try {
            // 双重检查：防止锁竞争期间已创建
            existingBucket = bucketMap.get(key);
            if (existingBucket != null) return existingBucket;
            // 检查是否超过最大Key数量
            while (keyOrderMap.size() >= maxKeyCount) {
                // 移除最早插入的Key
                String oldestKey = keyOrderMap.keySet().iterator().next();
                keyOrderMap.remove(oldestKey);
                bucketMap.remove(oldestKey);
                if (log.isWarnEnabled()) log.warn("Key数量已达上限({})，清理最早的Key: {}", maxKeyCount, oldestKey);
            }
            // 创建新令牌桶
            TokenBucket newBucket = new TokenBucket(ratePerSecond, tokenTotalMs);
            bucketMap.put(key, newBucket);
            keyOrderMap.put(key, newBucket);
            if (log.isDebugEnabled()) log.debug("创建新的令牌桶: {}", key);
            return newBucket;
        } finally {
            keyLock.unlock();
        }
    }

    /**
     * 阻塞式获取令牌
     * @param key 限流标识
     * @param ratePerSecond 时间窗口内最大请求数
     * @throws InterruptedException 线程被中断时抛出
     */
    public void acquire(String key, int ratePerSecond) throws InterruptedException {
        if (ratePerSecond <= 0) throw new IllegalArgumentException("速率必须大于0");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key不能为空");
        // 获取或创建令牌桶
        TokenBucket bucket = getOrCreateBucket(key, ratePerSecond);
        long requestStartMs = System.currentTimeMillis();
        while (true) {
            // 尝试获取独占锁（100ms超时，避免死等）
            if (bucket.lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                try {
                    // 尝试获取令牌
                    if (bucket.tryTakeToken()) {
                        if (log.isDebugEnabled()) {
                            long now = System.currentTimeMillis();
                            long waitMs = now - requestStartMs;
                            log.debug("[限流通过] Key: {}, 通过时间: {}, 等待时间: {}ms, 下一个令牌时间: {}",
                                    key, now, waitMs, bucket.nextTokenTime);
                        }
                        return;
                    }
                    // 未到令牌生成时间：计算等待时长，释放锁后休眠
                    long waitMs = bucket.nextTokenTime - System.currentTimeMillis();
                    bucket.lock.unlock(); // 先释放锁，避免持有锁休眠
                    Thread.sleep(waitMs);
                } finally {
                    // 确保锁最终被释放（避免异常导致锁泄漏）
                    if (bucket.lock.isHeldByCurrentThread()) {
                        bucket.lock.unlock();
                    }
                }
            } else {
                // 锁竞争：短暂休眠后重试（避免CPU空转）
                Thread.sleep(10);
                if (log.isTraceEnabled()) log.trace("[锁竞争] Key: {}, 等待获取令牌桶锁", key);
            }
        }
    }

    /**
     * 非阻塞式尝试获取令牌
     * @param key 限流标识
     * @param ratePerSecond 时间窗口内最大请求数
     * @param waitTime 尝试等待获取的时间
     *
     * @return true=获取成功，false=获取失败（未到令牌时间/锁竞争超时）
     */
    public boolean tryAcquire(String key, int ratePerSecond, Duration waitTime) {
        return tryAcquire(key, ratePerSecond, waitTime.toMillis());
    }

    /**
     * 非阻塞式尝试获取令牌
     * @param key 限流标识
     * @param ratePerSecond 时间窗口内最大请求数
     * @return true=获取成功，false=获取失败（未到令牌时间/锁竞争超时）
     */
    public boolean tryAcquire(String key, int ratePerSecond) {
        return tryAcquire(key, ratePerSecond, 0);
    }

    /**
     * 非阻塞式尝试获取令牌
     * @param key 限流标识
     * @param ratePerSecond 时间窗口内最大请求数
     * @param waitMs 尝试等待获取的时间（毫秒）
     * @return true=获取成功，false=获取失败（未到令牌时间/锁竞争超时）
     */
    public boolean tryAcquire(String key, int ratePerSecond, long waitMs) {
        if (isShutdown) {
            if (log.isWarnEnabled()) log.warn("限流器已关闭，Key[{}]获取令牌失败", key);
            return false;
        }
        if (ratePerSecond <= 0) throw new IllegalArgumentException("速率必须大于0");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key不能为空");
        // 获取或创建令牌桶
        TokenBucket bucket = getOrCreateBucket(key, ratePerSecond);
        // 尝试获取锁
        try {
            if (bucket.lock.tryLock(waitMs, TimeUnit.MILLISECONDS)) {
                try {
                    // 尝试获取令牌
                    boolean success = bucket.tryTakeToken();
                    if (success && log.isDebugEnabled()) {
                        log.debug("[非阻塞限流通过] Key: {}, 下一个令牌时间: {}",
                                key, bucket.nextTokenTime);
                    } else if (!success && log.isTraceEnabled()) {
                        log.trace("[非阻塞限流失败] Key: {}, 未到令牌时间（当前: {}, 下一个: {}）",
                                key, System.currentTimeMillis(), bucket.nextTokenTime);
                    }
                    return success;
                } finally {
                    // 确保锁释放
                    if (bucket.lock.isHeldByCurrentThread()) {
                        bucket.lock.unlock();
                    }
                }
            } else {
                // 锁竞争超时，返回失败
                if (log.isTraceEnabled()) log.trace("[非阻塞锁竞争超时] Key: {}, 获取令牌桶锁失败", key);
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 保留中断状态
            return false;
        }
    }

    /**
     * 清理逻辑：遍历所有令牌桶，删除过期未访问的桶
     */
    private void cleanupInactiveBuckets() {
        while (!isShutdown) {
            try {
                // 1. 休眠指定周期
                Thread.sleep(CLEANUP_PERIOD_MS);
                int cleanupCount = 0;
                // 加锁遍历有序Map
                keyLock.lock();
                try {
                    // 2. 遍历所有Key，检查令牌桶是否过期
                    for (Iterator<Map.Entry<String, TokenBucket>> iterator = keyOrderMap.entrySet().iterator(); iterator.hasNext(); ) {
                        Map.Entry<String, TokenBucket> entry = iterator.next();
                        String key = entry.getKey();
                        TokenBucket bucket = entry.getValue();
                        // 3. 尝试获取令牌桶锁
                        if (bucket.lock.tryLock(50, TimeUnit.MILLISECONDS)) {
                            try {
                                // 4. 判断是否过期，过期则删除
                                if (bucket.isInactive()) {
                                    iterator.remove();
                                    bucketMap.remove(key);
                                    cleanupCount++;
                                    if (log.isDebugEnabled()) log.debug("清理无效的令牌桶: {}", key);
                                }
                            } finally {
                                if (bucket.lock.isHeldByCurrentThread()) bucket.lock.unlock();
                            }
                        } else {
                            if (log.isTraceEnabled()) log.trace("[清理跳过] Key: {}, 令牌桶正在使用中", key);
                        }
                    }
                } finally {
                    keyLock.unlock();
                }
                if (cleanupCount > 0 && log.isDebugEnabled()) log.debug("定时清理完成：清理过期Key{}个", cleanupCount);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable e) {
                log.error("令牌桶清理异常：", e);
            }
        }
        if (log.isInfoEnabled()) log.info("令牌桶清理线程已停止");
    }

    /**
     * 关闭限流器：停止清理线程，清空所有令牌桶
     */
    @Override
    public void close() {
        if (isShutdown) return;
        isShutdown = true;
        if (cleanupThread.isAlive()) {
            cleanupThread.interrupt();
            try {
                cleanupThread.join(30000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        keyLock.lock();
        try {
            bucketMap.clear();
            keyOrderMap.clear();
        } finally {
            keyLock.unlock();
        }
    }

    /**
     * 重置指定Key的令牌桶
     */
    public void reset(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("Key不能为空");
        keyLock.lock();
        try {
            bucketMap.remove(key);
            keyOrderMap.remove(key);
            if (log.isDebugEnabled()) log.debug("重置令牌桶: {}", key);
        } finally {
            keyLock.unlock();
        }
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
        // 加锁更新，避免并发冲突
        oldBucket.lock.lock();
        try {
            // 创建新桶替换旧桶（保留lastAccessTime避免被误清理）
            TokenBucket newBucket = new TokenBucket(newRatePerSecond, tokenTotalMs);
            newBucket.lastAccessTime = oldBucket.lastAccessTime;
            newBucket.nextTokenTime = oldBucket.nextTokenTime; // 保留当前令牌生成进度
            keyLock.lock();
            try {
                bucketMap.put(key, newBucket);
                keyOrderMap.put(key, newBucket);
            } finally {
                keyLock.unlock();
            }
            if (log.isDebugEnabled()) log.debug("Key[{}]限流阈值更新：旧{} -> 新{}", key, oldBucket.maxTokens, newRatePerSecond);
        } finally {
            oldBucket.lock.unlock();
        }
    }

    /**
     * 获取当前活跃Key数量
     */
    public int activeKeyCount() {
        keyLock.lock();
        try {
            return keyOrderMap.size();
        } finally {
            keyLock.unlock();
        }
    }

    public record BucketStatus(int maxTokens, long lastAccessTime, long nextTokenTime, boolean isInactive) {}

    /**
     * 获取指定Key的当前限流配置与状态
     */
    public BucketStatus bucketStatus(String key) {
        if (key == null || key.isBlank()) return null;
        TokenBucket bucket = bucketMap.get(key);
        if (bucket == null) return null;
        bucket.lock.lock();
        try {
            return new BucketStatus(bucket.maxTokens, bucket.lastAccessTime, bucket.nextTokenTime, bucket.isInactive());
        } finally {
            bucket.lock.unlock();
        }
    }

    /**
     * 获取最大Key数量配置
     */
    public int maxKeyCount() {
        return maxKeyCount;
    }
}
