package org.fz.nettyx.action;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * The interface Channel write action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 12 /24/2021 3:34 PM
 */
@FunctionalInterface
public interface ChannelWriteAction {

    ChannelWriteAction DO_NOTHING = (ctx, msg, prom) -> {
    };

    void act(ChannelHandlerContext ctx, Object msg, ChannelPromise promise);
}
