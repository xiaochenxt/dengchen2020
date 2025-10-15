package io.github.dengchen2020.id;

import io.github.dengchen2020.id.exception.IdGeneratorException;
import io.github.dengchen2020.id.snowflake.SnowflakeIdGenerator;

import java.time.Instant;

/**
 * id生成辅助工具
 * @author xiaochen
 * @since 2024/7/1
 */
public class IdHelper {

    private static SnowflakeIdGenerator idGenInstance = null;

    public static SnowflakeIdGenerator getIdGenInstance() {
        return idGenInstance;
    }


    /**
     * 设置参数，程序仅初始化时执行一次
     */
    public static void setIdGenerator(SnowflakeIdGenerator generator) throws IdGeneratorException {
        if(idGenInstance == null) idGenInstance = generator;
    }

    /**
     * 生成新的Id
     * 调用本方法前，请确保调用了 setIdGenerator 方法做初始化。
     *
     * @return id
     */
    public static long nextId() throws IdGeneratorException {
        return idGenInstance.newLong();
    }

    /**
     * 从id中解析出时间
     * @param id
     * @return {@link Instant}
     */
    public static Instant extractTime(long id) {
        return idGenInstance.extractTime(id);
    }

    /**
     * 根据时间戳生成id
     * @param timestamp 时间戳
     * @return id
     */
    public static long newLongFromTimestamp(long timestamp) {
        return idGenInstance.newLongFromTimestamp(timestamp);
    }

    /**
     * 根据时间戳生成id
     * @param timestamp 时间戳
     * @param workId 机器id，为0则返回结果是同时间戳最小id
     * @return id
     */
    public static long newLongFromTimestamp(long timestamp, short workId) {
        return idGenInstance.newLongFromTimestamp(timestamp, workId);
    }

}

