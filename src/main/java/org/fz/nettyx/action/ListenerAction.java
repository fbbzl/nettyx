package org.fz.nettyx.action;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.fz.nettyx.exception.StopRedoException;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.util.ChannelState;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static org.fz.nettyx.util.ChannelState.getChannelState;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/6/10 12:43
 */
public interface ListenerAction {

    void act(ActionChannelFutureListener listener, ChannelFuture cf);

    static ListenerAction redo(
            Supplier<ChannelFuture> did,
            long                    delay,
            TimeUnit                unit)
    {
        return (ls, cf) -> cf.channel().eventLoop().schedule(() -> did.get().addListener(ls), delay, unit);
    }

    static ListenerAction redo(
            UnaryOperator<ChannelFuture> did,
            long                         delay,
            TimeUnit                     unit)
    {
        return (ls, cf) -> cf.channel().eventLoop().schedule(() -> did.apply(cf).addListener(ls), delay, unit);
    }

    static ListenerAction redo(
            Supplier<ChannelFuture> did,
            long                    delay,
            TimeUnit                unit,
            int                     maxRedoTimes)
    {
        return redo(did, delay, unit, maxRedoTimes, null);
    }

    static ListenerAction redo(
            Supplier<ChannelFuture> did,
            long                    delay,
            TimeUnit                unit,
            int                     maxRedoTimes,
            BiConsumer<? super ChannelFutureListener, ChannelFuture> afterMaxRedoTimes)
    {
        return (ls, cf) -> {
            try {
                checkState(ls, cf, maxRedoTimes, afterMaxRedoTimes);
            } catch (StopRedoException stopRedo) {
                return;
            }
            cf.channel().eventLoop().schedule(() -> did.get().addListener(ls), delay, unit);
        };
    }

    static ListenerAction redo(
            UnaryOperator<ChannelFuture> did,
            long                         delay,
            TimeUnit                     unit,
            int                          maxRedoTimes)
    {
        return redo(did, delay, unit, maxRedoTimes, null);
    }

    static ListenerAction redo(
            UnaryOperator<ChannelFuture> did,
            long                         delay,
            TimeUnit                     unit,
            int                          maxRedoTimes,
            BiConsumer<? super ChannelFutureListener, ChannelFuture> afterMaxRedoTimes)
    {
        return (ls, cf) -> {
            try {
                checkState(ls, cf, maxRedoTimes, afterMaxRedoTimes);
            } catch (StopRedoException stopRedo) {
                return;
            }
            cf.channel().eventLoop().schedule(() -> did.apply(cf).addListener(ls), delay, unit);
        };
    }

    static void checkState(
            ChannelFutureListener ls,
            ChannelFuture         cf,
            int                   maxRedoTimes,
            BiConsumer<? super ChannelFutureListener, ChannelFuture> afterMaxRedoTimes)
            throws StopRedoException
    {
        ChannelState state = getChannelState(cf);
        if (state != null) {
            // the first connect-action is also the redo type
            if (state.getConnectTimes() + 1 > maxRedoTimes - 1) {
                if (afterMaxRedoTimes != null) afterMaxRedoTimes.accept(ls, cf);
                throw new StopRedoException();
            } else state.increase(cf);
        }
    }

}
