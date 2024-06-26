package org.fz.nettyx.action;

import io.netty.channel.ChannelHandlerContext;

/**
 * The interface Channel message action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 12 /24/2021 3:19 PM
 */
@FunctionalInterface
public interface ChannelReadAction {

    ChannelReadAction DO_NOTHING = (ctx, msg) -> {
    };

    void act(ChannelHandlerContext ctx, Object msg);
}
