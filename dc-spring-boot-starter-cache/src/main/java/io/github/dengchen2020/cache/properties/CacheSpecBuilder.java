package io.github.dengchen2020.cache.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置
 * @author xiaochen
 * @since 2024/5/30
 */
@Validated
@ConfigurationProperties(prefix = "dc.cache")
public class CacheSpecBuilder {

    @Valid
    private Redis redis = new Redis();

    @Valid
    private Caffeine caffeine = new Caffeine();

    public Caffeine getCaffeine() {
        return caffeine;
    }

    public void setCaffeine(Caffeine caffeine) {
        this.caffeine = caffeine;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public static class Redis {

        /**
         * 缓存属性配置
         */
        private Map<String, CacheSpec> specs = new HashMap<>();

        /**
         * 多久后过期
         */
        @DurationMin(seconds = 1)
        private Duration expireTime = Duration.ofSeconds(60);

        /**
         * 将缓存保存/清除等操作与正在进行的 Spring 管理的事务同步，仅在成功事务的提交后阶段执行实际的缓存保存/清除操作
         */
        private boolean transactionAware = true;

        public void setSpecs(Map<String, CacheSpec> specs) {
            this.specs = specs;
        }

        public Map<String, CacheSpec> getSpecs() {
            return specs;
        }

        public Duration getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(Duration expireTime) {
            this.expireTime = expireTime;
        }

        public boolean isTransactionAware() {
            return transactionAware;
        }

        public void setTransactionAware(boolean transactionAware) {
            this.transactionAware = transactionAware;
        }

        public static class CacheSpec {
            /**
             * 多久后过期
             */
            private Duration expireTime;

            /**
             * 多久后过期
             */
            public Duration getExpireTime() {
                return expireTime;
            }

            public void setExpireTime(Duration expireTime) {
                this.expireTime = expireTime;
            }
        }

    }

    public static class Caffeine {

        /**
         * 缓存属性配置
         */
        private Map<String, CacheSpec> specs = new HashMap<>();

        /**
         * 多久后过期
         */
        @DurationMin(seconds = 1)
        private Duration expireTime = Duration.ofSeconds(60);

        /**
         * 最大缓存元素个数
         */
        @Min(1)
        private int max = 200;

        /**
         * 是否读取后一段时间过期，默认写入后一段时间过期
         */
        private boolean expireAfterAccess = false;

        /**
         * 是否使用软引用包装value（内存不足时会被垃圾回收），生成的缓存将使用 == 比较来确定值的相等性，详见：{@link com.github.benmanes.caffeine.cache.Caffeine#softValues()}
         */
        private boolean softValues = false;

        /**
         * 将缓存保存/清除等操作与正在进行的 Spring 管理的事务同步，仅在成功事务的提交后阶段执行实际的缓存保存/清除操作
         */
        private boolean transactionAware = true;

        public void setSpecs(Map<String, CacheSpec> specs) {
            this.specs = specs;
        }

        public Map<String, CacheSpec> getSpecs() {
            return specs;
        }

        public Duration getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(Duration expireTime) {
            this.expireTime = expireTime;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public boolean isExpireAfterAccess() {
            return expireAfterAccess;
        }

        public void setExpireAfterAccess(boolean expireAfterAccess) {
            this.expireAfterAccess = expireAfterAccess;
        }

        public boolean isSoftValues() {
            return softValues;
        }

        public void setSoftValues(boolean softValues) {
            this.softValues = softValues;
        }

        public boolean isTransactionAware() {
            return transactionAware;
        }

        public void setTransactionAware(boolean transactionAware) {
            this.transactionAware = transactionAware;
        }

        public static class CacheSpec {
            /**
             * 多久后过期
             */
            private Duration expireTime;

            /**
             * 最大缓存元素个数
             */
            private Integer max;

            /**
             * 是否读取后一段时间过期，默认写入后一段时间过期
             */
            private Boolean expireAfterAccess;

            /**
             * 是否使用软引用包装value（内存不足时会被垃圾回收），生成的缓存将使用 == 比较来确定值的相等性，详见：{@link com.github.benmanes.caffeine.cache.Caffeine#softValues()}
             */
            private Boolean softValues;

            /**
             * 是否读取后一段时间过期，默认写入后一段时间过期
             */
            public Boolean getExpireAfterAccess() {
                return expireAfterAccess;
            }

            public void setExpireAfterAccess(Boolean expireAfterAccess) {
                this.expireAfterAccess = expireAfterAccess;
            }

            /**
             * 多久后过期
             */
            public Duration getExpireTime() {
                return expireTime;
            }

            public void setExpireTime(Duration expireTime) {
                this.expireTime = expireTime;
            }

            /**
             * 最大缓存元素个数
             */
            public Integer getMax() {
                return max;
            }

            public void setMax(Integer max) {
                this.max = max;
            }

            /**
             * 是否使用软引用包装value（内存不足时会被垃圾回收），生成的缓存将使用 == 比较来确定值的相等性，详见：{@link com.github.benmanes.caffeine.cache.Caffeine#softValues()}
             */
            public Boolean getSoftValues() {
                return softValues;
            }

            public void setSoftValues(Boolean softValues) {
                this.softValues = softValues;
            }
        }

    }

}