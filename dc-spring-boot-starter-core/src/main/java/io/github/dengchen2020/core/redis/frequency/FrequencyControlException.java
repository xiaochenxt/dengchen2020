package io.github.dengchen2020.core.redis.frequency;

import io.github.dengchen2020.core.exception.ViewToastException;

/**
 * 频控触发异常
 * @author xiaochen
 * @since 2025/5/15
 */
public class FrequencyControlException extends ViewToastException {

    /**
     * qps、qpm、qpd
     */
    private final String level;

    /**
     * 对应的频次
     */
    private final int num;

    public FrequencyControlException(final String level, final int num) {
        this("请求频次超限，请调整请求频次在自身频控范围内", level, num);
    }

    public FrequencyControlException(String message, String level, int num) {
        super(message, ViewToastException.CODE, null, false);
        this.level = level;
        this.num = num;
    }

    public String getLevel() {
        return level;
    }

    public int getNum() {
        return num;
    }

}
