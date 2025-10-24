package io.github.dengchen2020.id.snowflake;

import java.util.Objects;

/**
 * 雪花算法使用的参数
 *
 * @author xiaochen
 * @since 2024/7/2
 */
public class SnowflakeIdGeneratorOptions {

    public SnowflakeIdGeneratorOptions() {}

    public SnowflakeIdGeneratorOptions(short workerId) {
        this.workerId = workerId;
    }

    /**
     * 雪花计算方法
     * （1-漂移算法|2-传统算法），默认1
     */
    private short method = 1;

    /**
     * 基础时间（ms单位），一旦确定，不能再修改
     * 不能超过当前系统时间
     */
    private long baseTime = 1657209600000L;

    /**
     * 机器码
     * 必须由外部设定，最大值 2^workerIdBitLength-1
     */
    private short workerId = 0;

    /**
     * 机器码位长
     * 默认值6，取值范围 [1, 15]（要求：序列数位长+机器码位长不超过22）
     */
    private byte workerIdBitLength = 6;

    /**
     * 序列数位长
     * 默认值6，取值范围 [3, 21]（要求：序列数位长+机器码位长不超过22）
     */
    private byte seqBitLength = 6;

    /**
     * 最大序列数（含）
     * 设置范围 [minSeqNumber, 2^seqBitLength-1]，默认值0，表示最大序列数取最大值（2^SeqBitLength-1]）
     */
    private short maxSeqNumber = 0;

    /**
     * 最小序列数（含）
     * 默认值5，取值范围 [5, maxSeqNumber]，每毫秒的前5个序列数对应编号是0-4是保留位，其中1-4是时间回拨相应预留位，0是手工新值预留位
     */
    private short minSeqNumber = 5;

    /**
     * 最大漂移次数（含）
     * 默认2000，推荐范围500-10000（与计算能力有关）
     */
    private short topOverCostCount = 2000;

    public short getMethod() {
        return method;
    }

    public void setMethod(short method) {
        this.method = method;
    }

    public long getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(long baseTime) {
        this.baseTime = baseTime;
    }

    public short getWorkerId() {
        return workerId;
    }

    public void setWorkerId(short workerId) {
        this.workerId = workerId;
    }

    public byte getWorkerIdBitLength() {
        return workerIdBitLength;
    }

    public void setWorkerIdBitLength(byte workerIdBitLength) {
        this.workerIdBitLength = workerIdBitLength;
    }

    public byte getSeqBitLength() {
        return seqBitLength;
    }

    public void setSeqBitLength(byte seqBitLength) {
        this.seqBitLength = seqBitLength;
    }

    public short getMaxSeqNumber() {
        return maxSeqNumber;
    }

    public void setMaxSeqNumber(short maxSeqNumber) {
        this.maxSeqNumber = maxSeqNumber;
    }

    public short getMinSeqNumber() {
        return minSeqNumber;
    }

    public void setMinSeqNumber(short minSeqNumber) {
        this.minSeqNumber = minSeqNumber;
    }

    public short getTopOverCostCount() {
        return topOverCostCount;
    }

    public void setTopOverCostCount(short topOverCostCount) {
        this.topOverCostCount = topOverCostCount;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SnowflakeIdGeneratorOptions that = (SnowflakeIdGeneratorOptions) o;
        return method == that.method && baseTime == that.baseTime && workerId == that.workerId && workerIdBitLength == that.workerIdBitLength && seqBitLength == that.seqBitLength && maxSeqNumber == that.maxSeqNumber && minSeqNumber == that.minSeqNumber && topOverCostCount == that.topOverCostCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, baseTime, workerId, workerIdBitLength, seqBitLength, maxSeqNumber, minSeqNumber, topOverCostCount);
    }

    public String toString() {
        return "SnowflakeIdGeneratorOptions{" +
                "method=" + method +
                ", baseTime=" + baseTime +
                ", workerId=" + workerId +
                ", workerIdBitLength=" + workerIdBitLength +
                ", seqBitLength=" + seqBitLength +
                ", maxSeqNumber=" + maxSeqNumber +
                ", minSeqNumber=" + minSeqNumber +
                ", topOverCostCount=" + topOverCostCount +
                '}';
    }
}

