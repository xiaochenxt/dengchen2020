package io.github.dengchen2020.core.context;

/**
 * 上下文持有者
 * @author xiaochen
 * @since 2026/1/20
 */
public final class CtxHolder {

    private static final ScopedValue<Ctx> ctx = ScopedValue.newInstance();

    /**
     * 如果没有值会抛出异常，需确保是在{@link CtxHolder#runWith(Ctx, Runnable)}调用链中使用
     */
    public static Ctx ctx() {
        return ctx.get();
    }

    public static boolean hasCtx() {
        return ctx.isBound();
    }

    public static void runWith(Ctx value, Runnable task) {
        ScopedValue.where(ctx, value).run(task);
    }

    public static <R, X extends Throwable> R callWith(Ctx value, ScopedValue.CallableOp<? extends R, X> task) throws X {
        return ScopedValue.where(ctx, value).call(task);
    }

}
