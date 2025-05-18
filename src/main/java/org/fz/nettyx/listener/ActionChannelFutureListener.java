package org.fz.nettyx.listener;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.fz.nettyx.action.ListenerAction;

/**
 * The type Actionable channel future listener.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /2/4 17:48
 */
@Setter
@Accessors(chain = true, fluent = true)
public class ActionChannelFutureListener implements ChannelFutureListener {
    /**
     * When the different state of the channel is monitored, the corresponding method will be called
     */
    private ListenerAction
            whenSuccess,
            whenFailure,
            whenCancelled,
            whenDone;

    @Override
    public final void operationComplete(ChannelFuture cf)
    {
        if (whenDone      != null && cf.isDone())        whenDone.act(this, cf);
        if (whenSuccess   != null && cf.isSuccess())     whenSuccess.act(this, cf);
        if (whenFailure   != null && cf.cause() != null) whenFailure.act(this, cf);
        if (whenCancelled != null && cf.isCancelled())   whenCancelled.act(this, cf);
    }

}
