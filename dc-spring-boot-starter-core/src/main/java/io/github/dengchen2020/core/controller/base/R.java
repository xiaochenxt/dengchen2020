package io.github.dengchen2020.core.controller.base;

/**
 * 请求响应处理
 *
 * @param code 响应状态码 0-成功 400-失败，弹窗提示 500-异常
 * @param msg  响应信息
 * @param data 响应数据
 * @author xiaochen
 * @since 2023/1/9
 */
public record R<T>(int code, String msg, T data) {

    /**
     * 成功响应
     *
     * @param data 内容
     * @param <T>  成功响应体
     */
    public static <T> R<T> ok(T data) {
        return new R<>(0, "", data);
    }

    /**
     * 成功响应
     */
    public static <T> R<T> ok(String msg) {
        return new R<>(0, "", null);
    }

    /**
     * 成功响应
     *
     * @param <T> 正确响应体
     * @return 成功响应体
     */
    public static <T> R<T> ok(String msg, T data) {
        return new R<>(0, msg, data);
    }

    /**
     * 异常响应
     *
     * @param msg 异常提示
     * @return 异常响应体
     */
    public static R<String> fail(String msg) {
        return fail(400, msg, null);
    }

    /**
     * 异常响应
     *
     * @param code 异常状态码
     * @param msg  异常提示
     * @return 异常响应体
     */
    public static R<String> fail(int code, String msg) {
        return fail(code, msg, null);
    }

    /**
     * 异常响应
     *
     * @param code 异常状态码
     * @param msg  异常提示
     * @return 异常响应体
     */
    public static <T> R<T> fail(int code, String msg, T data) {
        return new R<>(code, msg, data);
    }

}
