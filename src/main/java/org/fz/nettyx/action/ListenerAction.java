package org.fz.nettyx.action;

import cn.hutool.core.thread.ThreadUtil;
import io.netty.channel.ChannelFuture;
import org.fz.nettyx.listener.ActionChannelFutureListener;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/6/10 12:43
 */
public interface ListenerAction {
    void act(ActionChannelFutureListener listener, ChannelFuture channelFuture) throws Exception;

    /**
     * will re-execute the action after assigned delay and timeUnit
     */
    static ListenerAction redo(Supplier<ChannelFuture> did, long delay, TimeUnit unit) {
        return (ls, cf) -> {
            ThreadUtil.safeSleep(unit.toMillis(delay));
            did.get().addListener(ls);
        };
    }

    static ListenerAction redo(UnaryOperator<ChannelFuture> did, long delay, TimeUnit unit) {
        return (ls, cf) -> {
            ThreadUtil.safeSleep(unit.toMillis(delay));
            did.apply(cf).addListener(ls);
        };
    }
}
