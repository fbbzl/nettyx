package org.fz.nettyx.support;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.function.ChannelFutureAction;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/2/4 17:48
 */
@Slf4j
@Setter
@Accessors(chain = true, fluent = true)
public class ActionableChannelFutureListener implements ChannelFutureListener {

    /**
     * When the different state of the channel is monitored, the corresponding method will be called
     */
    private ChannelFutureAction
        whenSuccess,
        whenFailure,
        whenCancel,
        whenDone;

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        // success
        if (channelFuture.isSuccess()) {
            act(whenSuccess, channelFuture);
        }

        // failed
        if (!channelFuture.isSuccess()) {
            act(whenFailure, channelFuture);
        }

        // done
        if (channelFuture.isDone()) {
            act(whenDone, channelFuture);
        }

        // canceled
        if (channelFuture.isCancelled()) {
            act(whenCancel, channelFuture);
        }
    }

    //********************************************      private start      ***************************************************//

    private void act(ChannelFutureAction channelFutureAction, ChannelFuture channelFuture) {
        if (channelFutureAction != null) {
            channelFutureAction.act(channelFuture);
        }
    }

    private String state(ChannelFuture channelFuture) {
        return channelFuture.isSuccess() ? "success" : "failed";
    }

    //********************************************      private end      ***************************************************//
}
