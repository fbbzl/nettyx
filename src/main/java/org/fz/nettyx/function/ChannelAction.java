package org.fz.nettyx.function;

import io.netty.channel.Channel;

/**
 * The interface Channel action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/21 14:09
 */
@FunctionalInterface
public interface ChannelAction extends Action {

    void act(Channel channel);
}
