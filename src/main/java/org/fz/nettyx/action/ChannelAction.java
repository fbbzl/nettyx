package org.fz.nettyx.action;

import io.netty.channel.Channel;

/**
 * The interface Channel action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/21 14:09
 */
@FunctionalInterface
public interface ChannelAction {

    /**
     * Act.
     *
     * @param channel the channel
     */
    void act(Channel channel);
}
