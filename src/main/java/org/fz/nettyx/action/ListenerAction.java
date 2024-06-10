package org.fz.nettyx.action;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.fz.nettyx.channel.ChannelState;
import org.fz.nettyx.listener.ActionChannelFutureListener;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.fz.nettyx.template.AbstractSingleChannelTemplate.CHANNEL_STATE_KEY;

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

    static ListenerAction redo(Supplier<ChannelFuture> did, long delay, TimeUnit unit, int maxRedoTimes) {
        return redo(did, delay, unit, maxRedoTimes, null);
    }

    static ListenerAction redo(
            Supplier<ChannelFuture> did,
            long delay, TimeUnit unit,
            int maxRedoTimes,
            BiConsumer<? super ActionChannelFutureListener, ChannelFuture> afterMaxRedoTimes) {
        return (ls, cf) -> {
            Channel channel = cf.channel();
            if (channel.hasAttr(CHANNEL_STATE_KEY)) {
                ChannelState state = channel.attr(CHANNEL_STATE_KEY).get();
                // the first connect-action was not the redo type
                if (state.getConnectTimes() > maxRedoTimes - 1) {
                    if (afterMaxRedoTimes != null) afterMaxRedoTimes.accept(ls, cf);
                    return;
                } else state.increase(cf);
            }
            channel.eventLoop().schedule(() -> did.get().addListener(ls), delay, unit);
        };
    }

    static ListenerAction redo(UnaryOperator<ChannelFuture> did, long delay, TimeUnit unit, int maxRedoTimes) {
        return redo(did, delay, unit, maxRedoTimes, null);
    }

    static ListenerAction redo(
            UnaryOperator<ChannelFuture> did,
            long delay,
            TimeUnit unit,
            int maxRedoTimes,
            BiConsumer<? super ActionChannelFutureListener, ChannelFuture> afterMaxRedoTimes) {
        return (ls, cf) -> {
            Channel channel = cf.channel();
            if (channel.hasAttr(CHANNEL_STATE_KEY)) {
                ChannelState state = channel.attr(CHANNEL_STATE_KEY).get();
                // the first connect-action was not the redo type
                if (state.getConnectTimes() > maxRedoTimes - 1) {
                    if (afterMaxRedoTimes != null) afterMaxRedoTimes.accept(ls, cf);
                    return;
                } else state.increase(cf);
            }
            channel.eventLoop().schedule(() -> did.apply(cf).addListener(ls), delay, unit);
        };
    }
}
