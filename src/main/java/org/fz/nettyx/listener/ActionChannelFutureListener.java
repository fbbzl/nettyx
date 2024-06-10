package org.fz.nettyx.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ListenerAction;

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
    public final void operationComplete(ChannelFuture channelFuture) throws Exception {
        if (channelFuture.isSuccess())     invokeAction(whenSuccess, this, channelFuture);
        if (channelFuture.cause() != null) invokeAction(whenFailure, this, channelFuture);
        if (channelFuture.isDone())        invokeAction(whenDone, this, channelFuture);
        if (channelFuture.isCancelled())   invokeAction(whenCancel, this, channelFuture);
    }

    public static void invokeAction(ListenerAction action, ActionChannelFutureListener listener, ChannelFuture cf) throws Exception {
        if (action != null) action.act(listener, cf);
    }

}
