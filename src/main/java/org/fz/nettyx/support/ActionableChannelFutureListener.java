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
            log.info("connect success, remote address is [{}]", channelFuture.channel().remoteAddress());

            act(whenSuccess, channelFuture);
        }

        // failed
        if (!channelFuture.isSuccess()) {
            log.warn("connect failed, {}", channelFuture.cause().getMessage());

            act(whenFailure, channelFuture);
        }

        // done
        if (channelFuture.isDone()) {
            log.debug("connect done, state is [{}], address is [{}]", state(channelFuture), channelFuture.channel().remoteAddress());

            act(whenDone, channelFuture);
        }

        // canceled
        if (channelFuture.isCancelled()) {
            log.info("connect cancelled, address is [{}]", channelFuture.channel().remoteAddress());

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
