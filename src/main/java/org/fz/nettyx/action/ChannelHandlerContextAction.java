package org.fz.nettyx.action;

import io.netty.channel.ChannelHandlerContext;

/**
 * The interface Channel handler context action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/26 11:09
 */
@FunctionalInterface
public interface ChannelHandlerContextAction {

    ChannelHandlerContextAction DO_NOTHING = ctx -> {};

    void act(ChannelHandlerContext ctx);
}
