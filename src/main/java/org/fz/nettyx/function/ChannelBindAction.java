package org.fz.nettyx.function;

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

    /**
     * Act.
     *
     * @param ctx          the ctx
     * @param localAddress the local address
     * @param promise      the promise
     */
    void act(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise);
}
