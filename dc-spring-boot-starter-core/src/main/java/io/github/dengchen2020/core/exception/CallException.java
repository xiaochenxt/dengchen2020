package io.github.dengchen2020.core.exception;

import org.slf4j.helpers.MessageFormatter;

/**
 * 调用异常-接口调用产生的异常
 *
 * @author xiaochen
 * @since 2019/10/17 11:03
 */
public class CallException extends BaseException {

    public static final int CODE = 101;

    /**
     * 是否告警
     */
    private final boolean alarm;

    public boolean isAlarm() {
        return alarm;
    }

    //直接实例化构造方法
    public CallException(String message, Throwable cause) {
        super(message, CODE, cause);
        this.alarm = true;
    }

    public CallException(String message) {
        this(message, (Throwable) null);
    }

    public CallException(String message, Throwable cause, boolean alarm) {
        super(message, CODE, cause);
        this.alarm = alarm;
    }

    public CallException(String message, boolean alarm) {
        this(message, null, alarm);
    }

    public CallException(String message, Object... args) {
        super(MessageFormatter.arrayFormat(message, args).getMessage(), CODE);
        this.alarm = true;
    }

    public CallException(Throwable cause, String message) {
        super(message, CODE, cause);
        this.alarm = false;
    }

    public CallException(Throwable cause, String message, Object... args) {
        super(MessageFormatter.arrayFormat(message, args).getMessage(), CODE, cause);
        this.alarm = false;
    }

    //子类继承构造方法
    protected CallException(String message, int code, Throwable cause) {
        super(message, code, cause);
        this.alarm = false;
    }

    protected CallException(String message, int code) {
        this(message, null, code);
    }
}
