package io.github.dengchen2020.core.redis.frequency;

import io.github.dengchen2020.core.utils.DateTimeUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 频控
 * @author xiaochen
 * @since 2025/5/14
 */
public class FrequencyControlSupport {

    private final StringRedisTemplate redisTemplate;

    private static final String FREQUENCY_CONTROL_PREFIX = "dc:frequency_control:";

    RedisScript<Long> script = new DefaultRedisScript<>(
            """ 
                    local qpdKey = KEYS[3]
                    local qpdNum = tonumber(ARGV[3])
                    if qpdNum > 0 then
                        local qpdValue = redis.call("GET", qpdKey)
                        if qpdValue then
                            local num = tonumber(qpdValue)
                            if num >= qpdNum then
                                return 3
                            end
                        else
                            local dayS = tonumber(ARGV[4])
                            redis.call("SET", qpdKey, "0", "EX", dayS)
                        end
                    end
                    local qpmKey = KEYS[2]
                    local qpmNum = tonumber(ARGV[2])
                    if qpmNum > 0 then
                        local qpmValue = redis.call("GET", qpmKey)
                        if qpmValue then
                            local num = tonumber(qpmValue)
                            if num >= qpmNum then
                                return 2
                            end
                        else
                            redis.call("SET", qpmKey, "0", "EX", 60)
                        end
                    end
                    local qpsKey = KEYS[1]
                    local qpsNum = tonumber(ARGV[1])
                    if qpsNum > 0 then
                        local qpsValue = redis.call("GET", qpsKey)
                        if qpsValue then
                            local num = tonumber(qpsValue)
                            if num >= qpsNum then
                                return 1
                            end
                        else
                            redis.call("SET", qpsKey, "0", "EX", 1)
                        end
                    end
                    if qpdNum > 0 then
                        redis.call("INCR", qpdKey)
                    end
                    if qpmNum > 0 then
                        redis.call("INCR", qpmKey)
                    end
                    if qpsNum > 0 then
                        redis.call("INCR", qpsKey)
                    end
                    return 0
                    """,
            Long.class
    );

    public FrequencyControlSupport(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 0-未触发频控 1-触发qps频控 2-触发qpm频控 3-触发qps频控
     * @return
     */
    public long trigger(String key, int qps, int qpm, int qpd) {
        if (qps <= 0 && qpm <= 0 && qpd <= 0) return 0;
        var slot = "{"+key+"}";
        String dayS;
        if (qpd <= 0) {
            dayS = "86400";
        } else {
            var now = LocalDateTime.now();
            dayS = String.valueOf(DateTimeUtils.betweenSecond(now, DateTimeUtils.beginOfNextDay(now)));
        }
        return redisTemplate.execute(script, List.of(FREQUENCY_CONTROL_PREFIX+"qps:"+slot, FREQUENCY_CONTROL_PREFIX+"qpm:"+slot, FREQUENCY_CONTROL_PREFIX+"qpd:"+slot), String.valueOf(qps), String.valueOf(qpm), String.valueOf(qpd), dayS);
    }

}
