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

    ChannelAction DO_NOTHING = chl -> {};

    void act(Channel channel);
}
