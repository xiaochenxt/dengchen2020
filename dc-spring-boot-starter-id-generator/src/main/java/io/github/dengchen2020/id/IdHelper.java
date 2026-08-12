package io.github.dengchen2020.id;

import io.github.dengchen2020.core.utils.Base36Utils;
import io.github.dengchen2020.core.utils.Base62Utils;
import io.github.dengchen2020.id.snowflake.SnowflakeIdGenerator;

import java.time.Instant;

/**
 * 全局唯一id生成辅助工具，长度保持在13-16位之间（默认配置70年内不超过js最大值）
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
     * 生成新的Id并转为36进制（数字加大写字母组合）
     * <p>调用本方法前，请确保调用了 setIdGenerator 方法做初始化。</p>
     *
     * @return Base62编码的id字符串
     */
    public static String nextIdBase36Upper() {
        return Base36Utils.encodeUpper(idGenInstance.newLong());
    }

    /**
     * 生成新的Id并转为36进制（数字加小写字母组合）
     * <p>调用本方法前，请确保调用了 setIdGenerator 方法做初始化。</p>
     *
     * @return Base62编码的id字符串
     */
    public static String nextIdBase36Lower() {
        return Base36Utils.encodeLower(idGenInstance.newLong());
    }

    /**
     * 生成新的Id并转为62进制（数字、小写、大写字母组合）
     * <p>调用本方法前，请确保调用了 setIdGenerator 方法做初始化。</p>
     *
     * @return Base62编码的id字符串
     */
    public static String nextIdBase62() {
        return Base62Utils.encode(idGenInstance.newLong());
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

