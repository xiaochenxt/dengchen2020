package io.github.dengchen2020.core.redis;

import io.github.dengchen2020.core.utils.hash.BloomFilter;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于Redis的布隆过滤器实现
 * <pre>
 * 使用Redis的SETBIT和GETBIT命令实现分布式布隆过滤器
 * 特点：
 * - 分布式共享：多个应用实例共享同一个过滤器
 * - 持久化：数据存储在Redis中，重启不丢失
 * - 线程安全：Redis命令天然线程安全
 * </pre>
 * @author xiaochen
 * @since 2026/2/5
 */
public class RedisBloomFilter extends BloomFilter {
    
    private final StringRedisTemplate redisTemplate;
    private final String key;
    
    /**
     * 创建Redis布隆过滤器（默认误判率0.01）
     * @param key Redis键名
     * @param redisTemplate StringRedisTemplate实例
     * @param expectedElements 预期元素数量
     */
    public RedisBloomFilter(String key, StringRedisTemplate redisTemplate, int expectedElements) {
        super(expectedElements);
        this.key = key;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 创建Redis布隆过滤器
     * @param key Redis键名
     * @param redisTemplate StringRedisTemplate实例
     * @param expectedElements 预期元素数量
     * @param fpp 期望的误判率
     */
    public RedisBloomFilter(String key, StringRedisTemplate redisTemplate, int expectedElements, double fpp) {
        super(expectedElements, fpp);
        this.key = key;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 直接指定参数创建Redis布隆过滤器
     * @param key Redis键名
     * @param redisTemplate StringRedisTemplate实例
     * @param bitSize 位数组大小
     * @param hashFunctions hash函数个数
     */
    public RedisBloomFilter(String key, StringRedisTemplate redisTemplate, int bitSize, int hashFunctions) {
        super(bitSize, hashFunctions);
        this.key = key;
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public void clear() {
        redisTemplate.delete(key);
    }
    
    @Override
    protected void setBit(int bitIndex) {
        redisTemplate.opsForValue().setBit(key, bitIndex, true);
    }
    
    @Override
    protected boolean getBit(int bitIndex) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, bitIndex));
    }
    
}
