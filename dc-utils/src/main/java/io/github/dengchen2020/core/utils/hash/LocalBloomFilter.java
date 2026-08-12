package io.github.dengchen2020.core.utils.hash;

import java.util.concurrent.atomic.AtomicLongArray;

/**
 * 内存版本的布隆过滤器实现（线程安全）
 * <pre>
 * 基于AtomicLongArray实现的线程安全布隆过滤器
 * 特点：
 * - 使用CAS操作保证线程安全
 * - 适用于单机内存场景
 * - 性能高效，无锁设计
 * </pre>
 * @author xiaochen
 * @since 2026/2/5
 */
public class LocalBloomFilter extends BloomFilter {
    
    /**
     * 位数组（使用AtomicLongArray保证线程安全）
     */
    private final AtomicLongArray bits;
    
    /**
     * 根据预期元素数量创建布隆过滤器（默认误判率0.01）
     * @param expectedElements 预期元素数量
     */
    public LocalBloomFilter(int expectedElements) {
        super(expectedElements);
        this.bits = new AtomicLongArray((bitSize + 63) / 64);
    }
    
    /**
     * 根据预期元素数量和误判率创建布隆过滤器
     * @param expectedElements 预期元素数量
     * @param fpp 期望的误判率
     */
    public LocalBloomFilter(int expectedElements, double fpp) {
        super(expectedElements, fpp);
        this.bits = new AtomicLongArray((bitSize + 63) / 64);
    }
    
    /**
     * 直接指定位数组大小和hash函数个数创建布隆过滤器
     * @param bitSize 位数组大小
     * @param hashFunctions hash函数个数
     */
    public LocalBloomFilter(int bitSize, int hashFunctions) {
        super(bitSize, hashFunctions);
        this.bits = new AtomicLongArray((bitSize + 63) / 64);
    }
    
    @Override
    public void clear() {
        for (int i = 0; i < bits.length(); i++) {
            bits.set(i, 0L);
        }
    }
    
    @Override
    protected void setBit(int bitIndex) {
        int longIndex = bitIndex >>> 6; // 除以64
        int bitOffset = bitIndex & 63;  // 模64
        long mask = 1L << bitOffset;
        
        // 使用CAS循环确保原子性
        long oldValue;
        long newValue;
        do {
            oldValue = bits.get(longIndex);
            newValue = oldValue | mask;
        } while (oldValue != newValue && !bits.compareAndSet(longIndex, oldValue, newValue));
    }
    
    @Override
    protected boolean getBit(int bitIndex) {
        int longIndex = bitIndex >>> 6;
        int bitOffset = bitIndex & 63;
        return (bits.get(longIndex) & (1L << bitOffset)) != 0;
    }
    
}
