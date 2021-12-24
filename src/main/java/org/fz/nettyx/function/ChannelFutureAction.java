package org.fz.nettyx.function;

import io.netty.channel.ChannelFuture;

/**
 * The interface Channel future action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/21 14:27
 */
@FunctionalInterface
public interface ChannelFutureAction extends Action<ChannelFuture> {

}
