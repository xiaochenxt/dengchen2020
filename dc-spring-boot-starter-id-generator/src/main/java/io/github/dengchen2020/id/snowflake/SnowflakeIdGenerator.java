package io.github.dengchen2020.id.snowflake;

import io.github.dengchen2020.id.exception.IdGeneratorException;

import java.time.Instant;

/**
 * 雪花算法生成器
 * @author xiaochen
 * @since 2024/7/1
 */
public class SnowflakeIdGenerator {

    private static SnowWorker _SnowWorker = null;

    public SnowflakeIdGenerator(SnowflakeIdGeneratorOptions options) throws IdGeneratorException {
        if (options == null) {
            throw new RuntimeException("雪花算法配置错误");
        }

        // 1.BaseTime
        if (options.getBaseTime() < 315504000000L || options.getBaseTime() > System.currentTimeMillis()) {
            throw new IdGeneratorException("基础时间（BaseTime）配置错误，取值范围：315504000000-"+System.currentTimeMillis());
        }

        // 2.WorkerIdBitLength
        if (options.getWorkerIdBitLength() <= 0) {
            throw new IdGeneratorException("序列数位长（WorkerIdBitLength）配置错误，取值范围：1-21");
        }
        if (options.getWorkerIdBitLength() + options.getSeqBitLength() > 22) {
            throw new IdGeneratorException("序列数位长（WorkerIdBitLength）配置错误，WorkerIdBitLength + SeqBitLength <= 22");
        }

        // 3.WorkerId
        int maxWorkerIdNumber = (1 << options.getWorkerIdBitLength()) - 1;
        if (maxWorkerIdNumber == 0) {
            maxWorkerIdNumber = 63;
        }
        if (options.getWorkerId() < 0 || options.getWorkerId() > maxWorkerIdNumber) {
            throw new IdGeneratorException("机器码（WorkerId）配置错误，取值范围：0-" + (maxWorkerIdNumber > 0 ? maxWorkerIdNumber : 63));
        }

        // 4.SeqBitLength
        if (options.getSeqBitLength() < 2 || options.getSeqBitLength() > 21) {
            throw new IdGeneratorException("序列数位长（SeqBitLength）配置错误，取值范围：2-21");
        }

        // 5.MaxSeqNumber
        int maxSeqNumber = (1 << options.getSeqBitLength()) - 1;
        if (maxSeqNumber == 0) {
            maxSeqNumber = 63;
        }
        if (options.getMaxSeqNumber() < 0 || options.getMaxSeqNumber() > maxSeqNumber) {
            throw new IdGeneratorException("最大序列数（含）（MaxSeqNumber）配置错误，取值范围：1-"+ maxSeqNumber);
        }

        // 6.MinSeqNumber
        if (options.getMinSeqNumber() < 5 || options.getMinSeqNumber() > maxSeqNumber) {
            throw new IdGeneratorException("最小序列数（含）（MinSeqNumber）配置错误，取值范围：5-" + maxSeqNumber);
        }

        switch (options.getMethod()) {
            case 2:
                _SnowWorker = new SnowWorker2(options);
                break;
            case 1:
            default:
                _SnowWorker = new SnowWorker(options);
                break;
        }

    }

    /**
     * 生成新的id
     * @return id
     */
    public long newLong() {
        return _SnowWorker.nextId();
    }

    /**
     * 从id中解析出时间
     * @param id
     * @return {@link Instant}
     */
    public Instant extractTime(long id) {
        long shiftBits = _SnowWorker.workerIdBitLength + _SnowWorker.seqBitLength;
        long timestamp = (id >> shiftBits) + _SnowWorker.baseTime;
        return Instant.ofEpochMilli(timestamp);
    }

    /**
     * 根据时间戳生成id
     * @param timestamp 时间戳
     * @return id
     */
    public long newLongFromTimestamp(long timestamp) {
        return newLongFromTimestamp(timestamp, _SnowWorker.workerId);
    }

    /**
     * 根据时间戳生成出对应时间戳的最小id
     * @param timestamp 时间戳
     * @return id
     */
    public long newLongMinFromTimestamp(long timestamp) {
        return newLongFromTimestamp(timestamp, (short) 0);
    }

    /**
     * 根据时间戳和机器ID生成id
     * @param timestamp 时间戳
     * @param workId 机器id，为0则返回结果是同时间戳最小id
     * @return id
     */
    public long newLongFromTimestamp(long timestamp, short workId) {
        long timePart = (timestamp - _SnowWorker.baseTime) << (_SnowWorker.workerIdBitLength + _SnowWorker.seqBitLength);
        long workerPart = (long) workId << _SnowWorker.seqBitLength;
        return timePart | workerPart;
    }
}


