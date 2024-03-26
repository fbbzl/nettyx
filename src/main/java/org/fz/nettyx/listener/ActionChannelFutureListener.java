package org.fz.nettyx.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * The type Actionable channel future listener.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /2/4 17:48
 */
@Slf4j
@Setter
@Accessors(chain = true, fluent = true)
public class ActionChannelFutureListener implements ChannelFutureListener {

    /**
     * When the different state of the channel is monitored, the corresponding method will be called
     */
    private ListenerAction
            whenSuccess,
            whenFailure,
            whenCancel,
            whenDone;

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        // success
        if (channelFuture.isSuccess()) invokeAction(whenSuccess, this, channelFuture);
        else
        // failed
        if (!channelFuture.isSuccess()) invokeAction(whenFailure, this, channelFuture);

        // done
        if (channelFuture.isDone()) invokeAction(whenDone, this, channelFuture);
        else
        // canceled
        if (channelFuture.isCancelled()) invokeAction(whenCancel, this, channelFuture);
    }

    public static ListenerAction reconnect(Supplier<ChannelFuture> connect, long delay, TimeUnit unit) {
        return (ls, cf) -> {
            cf.channel().eventLoop().schedule(() -> connect.get().addListener(ls), delay, unit);
        };
    }


    public interface ListenerAction {
        void act(ChannelFutureListener listener, ChannelFuture channelFuture);
    }

    public static void invokeAction(ListenerAction action, ChannelFutureListener listener, ChannelFuture cf) {
        if (action != null) {
            action.act(listener, cf);
        }
    }

}
