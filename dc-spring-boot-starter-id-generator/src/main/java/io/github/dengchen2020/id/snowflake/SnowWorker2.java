package io.github.dengchen2020.id.snowflake;

import io.github.dengchen2020.id.exception.IdGeneratorException;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 雪花算法-传统算法
 * @author xiaochen
 * @since 2024/7/1
 */
class SnowWorker2 extends SnowWorker {

    public SnowWorker2(SnowflakeIdGeneratorOptions options) {
        super(options);
    }

    private static final ReentrantLock lock = new ReentrantLock();

    @Override
    public long nextId() {
        lock.lock();

        try {
            long currentTimeTick = GetCurrentTimeTick();

            if (_LastTimeTick == currentTimeTick) {
                if (_CurrentSeqNumber++ > maxSeqNumber) {
                    _CurrentSeqNumber = minSeqNumber;
                    currentTimeTick = GetNextTimeTick();
                }
            } else {
                _CurrentSeqNumber = minSeqNumber;
            }

            if (currentTimeTick < _LastTimeTick) {
                throw new IdGeneratorException("Time配置错误 %d 毫秒", _LastTimeTick - currentTimeTick);
            }

            _LastTimeTick = currentTimeTick;

            return ((currentTimeTick << _TimestampShift) + ((long) workerId << seqBitLength) + (int) _CurrentSeqNumber);
        } finally {
            lock.unlock();
        }

    }
}
