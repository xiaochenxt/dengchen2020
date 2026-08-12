package io.github.dengchen2020.id.snowflake;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 雪花算法-漂移算法
 */
class SnowWorker {

    /**
     * 基础时间
     */
    protected final long baseTime;

    /**
     * 机器码
     */
    protected final short workerId;

    /**
     * 机器码位长
     */
    protected final byte workerIdBitLength;

    /**
     * 自增序列数位长
     */
    protected final byte seqBitLength;

    /**
     * 最大序列数（含）
     */
    protected final int maxSeqNumber;

    /**
     * 最小序列数（含）
     */
    protected final short minSeqNumber;

    /**
     * 最大漂移次数
     */
    protected final int topOverCostCount;

    protected final byte _TimestampShift;

    private final ReentrantLock lock = new ReentrantLock();

    protected short _CurrentSeqNumber;
    protected long _LastTimeTick = 0;
    protected long _TurnBackTimeTick = 0;
    protected byte _TurnBackIndex = 0;

    protected boolean _IsOverCost = false;
    protected int _OverCostCountInOneTerm = 0;
    protected int _GenCountInOneTerm = 0;
    protected int _TermIndex = 0;

    public SnowWorker(SnowflakeIdGeneratorOptions options) {
        baseTime = options.getBaseTime() != 0 ? options.getBaseTime() : 1657209600000L;
        workerIdBitLength = options.getWorkerIdBitLength() == 0 ? 6 : options.getWorkerIdBitLength();
        workerId = options.getWorkerId();
        seqBitLength = options.getSeqBitLength() == 0 ? 6 : options.getSeqBitLength();
        maxSeqNumber = options.getMaxSeqNumber() <= 0 ? (1 << seqBitLength) - 1 : options.getMaxSeqNumber();
        minSeqNumber = options.getMinSeqNumber();
        topOverCostCount = options.getTopOverCostCount() == 0 ? 2000 : options.getTopOverCostCount();
        _TimestampShift = (byte) (workerIdBitLength + seqBitLength);
        _CurrentSeqNumber = minSeqNumber;
    }

    private void endOverCostAction(long useTimeTick) {
        if (_TermIndex > 10000) {
            _TermIndex = 0;
        }
    }

    private long nextOverCostId() {
        long currentTimeTick = getCurrentTimeTick();

        if (currentTimeTick > _LastTimeTick) {
            endOverCostAction(currentTimeTick);

            _LastTimeTick = currentTimeTick;
            _CurrentSeqNumber = minSeqNumber;
            _IsOverCost = false;
            _OverCostCountInOneTerm = 0;
            _GenCountInOneTerm = 0;

            return calcId(_LastTimeTick);
        }

        if (_OverCostCountInOneTerm >= topOverCostCount) {
            endOverCostAction(currentTimeTick);

            _LastTimeTick = getNextTimeTick();
            _CurrentSeqNumber = minSeqNumber;
            _IsOverCost = false;
            _OverCostCountInOneTerm = 0;
            _GenCountInOneTerm = 0;

            return calcId(_LastTimeTick);
        }

        if (_CurrentSeqNumber > maxSeqNumber) {
            _LastTimeTick++;
            _CurrentSeqNumber = minSeqNumber;
            _IsOverCost = true;
            _OverCostCountInOneTerm++;
            _GenCountInOneTerm++;

            return calcId(_LastTimeTick);
        }

        _GenCountInOneTerm++;
        return calcId(_LastTimeTick);
    }

    private long nextNormalId() {
        long currentTimeTick = getCurrentTimeTick();

        if (currentTimeTick < _LastTimeTick) {
            if (_TurnBackTimeTick < 1) {
                _TurnBackTimeTick = _LastTimeTick - 1;
                _TurnBackIndex++;

                // 每毫秒序列数的前5位是预留位，0用于手工新值，1-4是时间回拨次序
                // 最多4次回拨（防止回拨重叠）
                if (_TurnBackIndex > 4) {
                    _TurnBackIndex = 1;
                }
            }

            return calcTurnBackId(_TurnBackTimeTick);
        }

        // 时间追平时，_TurnBackTimeTick清零
        if (_TurnBackTimeTick > 0) {
            _TurnBackTimeTick = 0;
        }

        if (currentTimeTick > _LastTimeTick) {
            _LastTimeTick = currentTimeTick;
            _CurrentSeqNumber = minSeqNumber;

            return calcId(_LastTimeTick);
        }

        if (_CurrentSeqNumber > maxSeqNumber) {

            _TermIndex++;
            _LastTimeTick++;
            _CurrentSeqNumber = minSeqNumber;
            _IsOverCost = true;
            _OverCostCountInOneTerm = 1;
            _GenCountInOneTerm = 1;

            return calcId(_LastTimeTick);
        }

        return calcId(_LastTimeTick);
    }

    private long calcId(long useTimeTick) {
        long result = ((useTimeTick << _TimestampShift) +
                ((long) workerId << seqBitLength) +
                (int) _CurrentSeqNumber);

        _CurrentSeqNumber++;
        return result;
    }

    private long calcTurnBackId(long useTimeTick) {
        long result = ((useTimeTick << _TimestampShift) +
                ((long) workerId << seqBitLength) + _TurnBackIndex);

        _TurnBackTimeTick--;
        return result;
    }

    protected long getCurrentTimeTick() {
        long millis = System.currentTimeMillis();
        return millis - baseTime;
    }

    protected long getNextTimeTick() {
        long tempTimeTicker = getCurrentTimeTick();

        while (tempTimeTicker <= _LastTimeTick) {
            try {
                // 直接等待相差的时间，避免频繁且无效的唤醒检查
                Thread.sleep(_LastTimeTick - tempTimeTicker);
            } catch (InterruptedException _) {}
            tempTimeTicker = getCurrentTimeTick();
        }

        return tempTimeTicker;
    }

    public long nextId() {
        lock.lock();
        try {
            return _IsOverCost ? nextOverCostId() : nextNormalId();
        } finally {
            lock.unlock();
        }
    }
}
