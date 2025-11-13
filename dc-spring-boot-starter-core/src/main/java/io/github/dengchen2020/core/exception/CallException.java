package io.github.dengchen2020.core.exception;

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

    public CallException(String message) {
        super(message, CODE);
        this.alarm = true;
    }

    public CallException(String message, Throwable cause) {
        super(message, CODE, cause);
        this.alarm = true;
    }

    protected CallException(String message, int code) {
        super(message, code);
        this.alarm = true;
    }

    protected CallException(String message, int code, Throwable cause) {
        super(message, code, cause);
        this.alarm = true;
    }

    public CallException(String message, Throwable cause, boolean alarm) {
        super(message, CODE, cause);
        this.alarm = alarm;
    }

    public CallException(String message, boolean alarm) {
        super(message, CODE);
        this.alarm = alarm;
    }

}
