package io.github.dengchen2020.id;

import io.github.dengchen2020.id.snowflake.SnowflakeIdGenerator;

import java.time.Instant;

/**
 * id生成辅助工具
 * @author xiaochen
 * @since 2024/7/1
 */
public class IdHelper {

    private static SnowflakeIdGenerator idGenInstance = null;


    /**
     * 设置参数，程序仅初始化时执行一次
     * <p>注意：如果项目中引入了spring-boot-starter-data-redis，不需要调用该方法，且该方法调用无效</p>
     */
    public static void setIdGenerator(SnowflakeIdGenerator generator) {
        if(idGenInstance == null) idGenInstance = generator;
    }

    /**
     * 生成新的Id
     * <p>调用本方法前，请确保调用了 setIdGenerator 方法做初始化。</p>
     *
     * @return id
     */
    public static long nextId() {
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
     * 根据时间戳生成id（同一时间戳生成的id相同）
     * @param timestamp 时间戳
     * @return id
     */
    public static long newLongFromTimestamp(long timestamp) {
        return idGenInstance.newLongFromTimestamp(timestamp, (short) 0);
    }

}

