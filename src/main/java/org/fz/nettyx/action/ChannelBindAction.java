package org.fz.nettyx.action;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

/**
 * The interface Channel bind action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 12 /24/2021 3:26 PM
 */
@FunctionalInterface
public interface ChannelBindAction {

    ChannelBindAction DO_NOTHING = (ctx, laddr, prom) -> {};

    void act(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise);
}
