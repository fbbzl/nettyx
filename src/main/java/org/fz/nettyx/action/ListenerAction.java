package org.fz.nettyx.action;

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

    static ListenerAction redo(Supplier<ChannelFuture> did, long delay, TimeUnit unit) {
        return (ls, cf) -> cf.channel().eventLoop().schedule(() -> did.get().addListener(ls), delay, unit);
    }

    static ListenerAction redo(UnaryOperator<ChannelFuture> did, long delay, TimeUnit unit) {
        return (ls, cf) -> cf.channel().eventLoop().schedule(() -> did.apply(cf).addListener(ls), delay, unit);
    }

    static ListenerAction redo(UnaryOperator<ChannelFuture> did, long delay, TimeUnit unit, int maxRedoTimes) {
        return (ls, cf) -> cf.channel().eventLoop().schedule(() -> did.apply(cf).addListener(ls), delay, unit);
    }
}
