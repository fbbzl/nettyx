package org.fz.nettyx.action;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * The interface Channel promise action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 12 /24/2021 3:31 PM
 */
@FunctionalInterface
public interface ChannelPromiseAction {

    ChannelPromiseAction DO_NOTHING = (ctx, prom) -> {};

    void act(ChannelHandlerContext ctx, ChannelPromise promise);

}
