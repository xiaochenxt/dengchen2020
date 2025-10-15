package io.github.dengchen2020.id.properties;

import io.github.dengchen2020.id.snowflake.SnowflakeIdGeneratorOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * id生成器属性配置
 * @author xiaochen
 * @since 2024/7/2
 */
@ConfigurationProperties(prefix = "dc.id")
public class IdGeneratorBuilder {

    /**
     * id类型
     */
    private Type type = Type.snowflake;

    /**
     * 雪花算法参数配置
     */
    private SnowflakeIdGeneratorOptions snowflake = new SnowflakeIdGeneratorOptions();

    public enum Type {
        /**
         * 雪花算法
         */
        snowflake
    }

    @ConfigurationProperties(prefix = "dc.id.snowflake")
    public SnowflakeIdGeneratorOptions getSnowflake() {
        return snowflake;
    }

    public void setSnowflake(SnowflakeIdGeneratorOptions snowflake) {
        this.snowflake = snowflake;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
