package io.github.dengchen2020.core.utils.hash;

/**
 * MurmurHash3算法实现（非加密hash，速度快，分布均匀）
 * <pre>
 * MurmurHash3是一种非加密型哈希函数，适用于一般的哈希检索操作
 * 特点：
 * - 速度快：比MD5、SHA等加密hash快得多
 * - 分布均匀：碰撞率低，适合哈希表
 * - 提供32位和128位两种版本
 * </pre>
 * @author xiaochen
 * @since 2026/2/5
 */
public abstract class MurmurHash3 {
    
    /**
     * MurmurHash3 32位实现
     * @param data 待hash数据
     * @param seed 种子值
     * @return 32位hash值
     */
    public static int hash32(byte[] data, int seed) {
        int h = seed;
        int len = data.length;
        int i = 0;
        
        // 处理4字节块
        while (i + 4 <= len) {
            int k = (data[i] & 0xff)
                    | ((data[i + 1] & 0xff) << 8)
                    | ((data[i + 2] & 0xff) << 16)
                    | ((data[i + 3] & 0xff) << 24);
            
            k *= 0xcc9e2d51;
            k = Integer.rotateLeft(k, 15);
            k *= 0x1b873593;
            
            h ^= k;
            h = Integer.rotateLeft(h, 13);
            h = h * 5 + 0xe6546b64;
            
            i += 4;
        }
        
        // 处理剩余字节
        int k = 0;
        switch (len - i) {
            case 3:
                k ^= (data[i + 2] & 0xff) << 16;
            case 2:
                k ^= (data[i + 1] & 0xff) << 8;
            case 1:
                k ^= (data[i] & 0xff);
                k *= 0xcc9e2d51;
                k = Integer.rotateLeft(k, 15);
                k *= 0x1b873593;
                h ^= k;
        }
        
        // 最终混合
        h ^= len;
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;
        
        return h;
    }
    
    /**
     * MurmurHash3 128位实现（x64版本）
     * @param data 待hash数据
     * @param seed 种子值
     * @return 包含两个long值的数组（128位hash）
     */
    public static long[] hash128(byte[] data, int seed) {
        long h1 = seed & 0xFFFFFFFFL;
        long h2 = seed & 0xFFFFFFFFL;
        
        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;
        
        int len = data.length;
        int i = 0;
        
        // 处理16字节块
        while (i + 16 <= len) {
            long k1 = getLong(data, i);
            long k2 = getLong(data, i + 8);
            
            k1 *= c1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= c2;
            h1 ^= k1;
            
            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;
            
            k2 *= c2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= c1;
            h2 ^= k2;
            
            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
            
            i += 16;
        }
        
        // 处理剩余字节
        long k1 = 0;
        long k2 = 0;
        
        switch (len - i) {
            case 15: k2 ^= ((long) data[i + 14] & 0xff) << 48;
            case 14: k2 ^= ((long) data[i + 13] & 0xff) << 40;
            case 13: k2 ^= ((long) data[i + 12] & 0xff) << 32;
            case 12: k2 ^= ((long) data[i + 11] & 0xff) << 24;
            case 11: k2 ^= ((long) data[i + 10] & 0xff) << 16;
            case 10: k2 ^= ((long) data[i + 9] & 0xff) << 8;
            case 9:  k2 ^= ((long) data[i + 8] & 0xff);
                     k2 *= c2;
                     k2 = Long.rotateLeft(k2, 33);
                     k2 *= c1;
                     h2 ^= k2;
            case 8:  k1 ^= ((long) data[i + 7] & 0xff) << 56;
            case 7:  k1 ^= ((long) data[i + 6] & 0xff) << 48;
            case 6:  k1 ^= ((long) data[i + 5] & 0xff) << 40;
            case 5:  k1 ^= ((long) data[i + 4] & 0xff) << 32;
            case 4:  k1 ^= ((long) data[i + 3] & 0xff) << 24;
            case 3:  k1 ^= ((long) data[i + 2] & 0xff) << 16;
            case 2:  k1 ^= ((long) data[i + 1] & 0xff) << 8;
            case 1:  k1 ^= ((long) data[i] & 0xff);
                     k1 *= c1;
                     k1 = Long.rotateLeft(k1, 31);
                     k1 *= c2;
                     h1 ^= k1;
        }
        
        // 最终混合
        h1 ^= len;
        h2 ^= len;
        
        h1 += h2;
        h2 += h1;
        
        h1 = fmix64(h1);
        h2 = fmix64(h2);
        
        h1 += h2;
        h2 += h1;
        
        return new long[]{h1, h2};
    }
    
    /**
     * 从字节数组读取long值（小端序）
     */
    private static long getLong(byte[] data, int offset) {
        return ((long) data[offset] & 0xff)
                | (((long) data[offset + 1] & 0xff) << 8)
                | (((long) data[offset + 2] & 0xff) << 16)
                | (((long) data[offset + 3] & 0xff) << 24)
                | (((long) data[offset + 4] & 0xff) << 32)
                | (((long) data[offset + 5] & 0xff) << 40)
                | (((long) data[offset + 6] & 0xff) << 48)
                | (((long) data[offset + 7] & 0xff) << 56);
    }
    
    /**
     * 64位最终混合函数
     */
    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }
    
}
