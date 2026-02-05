package io.github.dengchen2020.core.utils.hash;

import java.nio.charset.StandardCharsets;

/**
 * 布隆过滤器抽象基类
 * <pre>
 * 布隆过滤器是一种空间效率极高的概率型数据结构，用于判断一个元素是否在集合中
 * 特点：
 * - 如果判断元素不存在，则一定不存在
 * - 如果判断元素存在，则可能存在误判
 * - 使用双重散列技术减少hash冲突概率
 * </pre>
 * @author xiaochen
 * @since 2026/2/5
 */
public abstract class BloomFilter {
    
    /**
     * 位数组大小（bit数）
     */
    protected final int bitSize;
    
    /**
     * hash函数个数
     */
    protected final int hashFunctions;
    
    /**
     * 是否使用128位hash算法（根据数据量自动选择）
     */
    private final boolean use128BitHash;
    
    /**
     * 根据预期元素数量创建布隆过滤器（默认误判率0.01）
     * @param expectedElements 预期元素数量
     */
    protected BloomFilter(int expectedElements) {
        this(expectedElements, 0.01);
    }
    
    /**
     * 根据预期元素数量和误判率创建布隆过滤器
     * @param expectedElements 预期元素数量
     * @param fpp 期望的误判率 (false positive probability)，范围 (0, 1)
     */
    protected BloomFilter(int expectedElements, double fpp) {
        if (expectedElements <= 0) {
            throw new IllegalArgumentException("Expected elements must be positive: " + expectedElements);
        }
        if (fpp <= 0 || fpp >= 1) {
            throw new IllegalArgumentException("False positive probability must be in range (0, 1): " + fpp);
        }
        
        this.bitSize = optimalBitSize(expectedElements, fpp);
        this.hashFunctions = optimalHashFunctions(expectedElements, bitSize);
        // 数据量超过1000万时使用128位hash，碰撞率更低
        this.use128BitHash = expectedElements > 10_000_000;
    }
    
    /**
     * 直接指定位数组大小和hash函数个数创建布隆过滤器
     * @param bitSize 位数组大小
     * @param hashFunctions hash函数个数
     */
    protected BloomFilter(int bitSize, int hashFunctions) {
        if (bitSize <= 0) {
            throw new IllegalArgumentException("Bit size must be positive: " + bitSize);
        }
        if (hashFunctions <= 0) {
            throw new IllegalArgumentException("Hash functions must be positive: " + hashFunctions);
        }
        
        this.bitSize = bitSize;
        this.hashFunctions = hashFunctions;
        // 位数组超过1亿bit时使用128位hash
        this.use128BitHash = bitSize > 100_000_000;
    }
    
    /**
     * 添加元素到过滤器
     * @param element 待添加的元素
     */
    public void add(String element) {
        if (element == null) {
            throw new IllegalArgumentException("Element cannot be null");
        }
        
        byte[] bytes = element.getBytes(StandardCharsets.UTF_8);
        int[] hashes = getHashes(bytes);
        
        for (int i = 0; i < hashFunctions; i++) {
            int bitIndex = (hashes[0] + i * hashes[1]) % bitSize;
            if (bitIndex < 0) {
                bitIndex += bitSize;
            }
            setBit(bitIndex);
        }
    }
    
    /**
     * 检查元素是否可能存在
     * @param element 待检查的元素
     * @return true表示可能存在，false表示一定不存在
     */
    public boolean mightContain(String element) {
        if (element == null) {
            return false;
        }
        
        byte[] bytes = element.getBytes(StandardCharsets.UTF_8);
        int[] hashes = getHashes(bytes);
        
        for (int i = 0; i < hashFunctions; i++) {
            int bitIndex = (hashes[0] + i * hashes[1]) % bitSize;
            if (bitIndex < 0) {
                bitIndex += bitSize;
            }
            if (!getBit(bitIndex)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 清空过滤器
     */
    public abstract void clear();
    
    /**
     * 设置指定位置的bit为1
     * @param bitIndex 位索引
     */
    protected abstract void setBit(int bitIndex);
    
    /**
     * 获取指定位置的bit值
     * @param bitIndex 位索引
     * @return true表示该位为1，false表示为0
     */
    protected abstract boolean getBit(int bitIndex);
    
    /**
     * 使用双重散列技术生成两个hash值
     * 双重散列：使用两个独立的hash函数组合生成k个hash值，减少计算开销
     * 根据数据量自动选择32位或128位hash算法
     * @param data 待hash的数据
     * @return 包含两个hash值的数组
     */
    private int[] getHashes(byte[] data) {
        if (use128BitHash) {
            return getHashes128(data);
        }
        return getHashes32(data);
    }
    
    /**
     * 使用32位MurmurHash3生成两个hash值（适用于千万级以下数据）
     * @param data 待hash的数据
     * @return 包含两个hash值的数组
     */
    private int[] getHashes32(byte[] data) {
        int hash1 = MurmurHash3.hash32(data, 0);
        int hash2 = MurmurHash3.hash32(data, hash1);
        return new int[]{hash1, hash2};
    }
    
    /**
     * 使用128位MurmurHash3生成两个hash值（适用于千万级以上数据）
     * @param data 待hash的数据
     * @return 包含两个hash值的数组
     */
    private int[] getHashes128(byte[] data) {
        long[] hash128 = MurmurHash3.hash128(data, 0);
        // 使用Long.hashCode将64位hash压缩为32位
        int hash1 = Long.hashCode(hash128[0]);
        int hash2 = Long.hashCode(hash128[1]);
        return new int[]{hash1, hash2};
    }
    
    /**
     * 计算最优的位数组大小
     * 公式: m = -n * ln(p) / (ln(2)^2)
     * @param n 预期元素数量
     * @param p 期望误判率
     * @return 最优位数组大小
     */
    private static int optimalBitSize(int n, double p) {
        double m = -n * Math.log(p) / (Math.log(2) * Math.log(2));
        return (int) Math.ceil(m);
    }
    
    /**
     * 计算最优的hash函数个数
     * 公式: k = (m/n) * ln(2)
     * @param n 预期元素数量
     * @param m 位数组大小
     * @return 最优hash函数个数
     */
    private static int optimalHashFunctions(int n, int m) {
        double k = ((double) m / n) * Math.log(2);
        return Math.max(1, (int) Math.round(k));
    }
    
    /**
     * 获取位数组大小
     * @return 位数组大小
     */
    public int getBitSize() {
        return bitSize;
    }
    
    /**
     * 获取hash函数个数
     * @return hash函数个数
     */
    public int getHashFunctions() {
        return hashFunctions;
    }
    
}
