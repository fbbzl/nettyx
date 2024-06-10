package org.fz.nettyx.action;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.fz.nettyx.channel.ChannelState;
import org.fz.nettyx.exception.StopRedoException;
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

    void act(ActionChannelFutureListener listener, ChannelFuture cf) throws Exception;

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
            try {
                checkState(cf, maxRedoTimes, afterMaxRedoTimes);
            } catch (StopRedoException stopRedo) {
                return;
            }

            cf.channel().eventLoop().schedule(() -> did.get().addListener(ls), delay, unit);
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
            try {
                checkState(cf, maxRedoTimes, afterMaxRedoTimes);
            } catch (StopRedoException stopRedo) {
                return;
            }

            cf.channel().eventLoop().schedule(() -> did.apply(cf).addListener(ls), delay, unit);
        };
    }

    static void checkState(ChannelFuture cf, int maxRedoTimes,
                           BiConsumer<? super ActionChannelFutureListener, ChannelFuture> afterMaxRedoTimes)
            throws StopRedoException {
        Channel chl = cf.channel();
        if (chl.hasAttr(CHANNEL_STATE_KEY)) {
            ChannelState state = chl.attr(CHANNEL_STATE_KEY).get();
            // the first connect-action is also the redo type
            if (state.getConnectTimes() + 1 > maxRedoTimes - 1) {
                if (afterMaxRedoTimes != null) afterMaxRedoTimes.accept(ls, cf);
                throw new StopRedoException();
            } else state.increase(cf);
        }
    }

}
