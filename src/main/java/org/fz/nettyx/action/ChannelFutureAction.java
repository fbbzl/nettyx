package org.fz.nettyx.action;

import io.netty.channel.ChannelFuture;

/**
 * The interface Channel future action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/21 14:27
 */
@FunctionalInterface
public interface ChannelFutureAction {

    ChannelFutureAction DO_NOTHING = cf -> {};

    /**
     * Act.
     *
     * @param channelFuture the channel future
     */
    void act(ChannelFuture channelFuture);


}
