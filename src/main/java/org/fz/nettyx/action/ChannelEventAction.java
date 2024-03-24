package org.fz.nettyx.action;

import io.netty.channel.ChannelHandlerContext;

/**
 * The interface Channel event action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /6/17 10:50
 */
@FunctionalInterface
public interface ChannelEventAction {

    ChannelEventAction DO_NOTHING = (ctx, evt) -> {};

    void act(ChannelHandlerContext ctx, Object evt);

}
