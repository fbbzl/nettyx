package org.fz.nettyx.action;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

/**
 * The interface Channel connect action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 12 /24/2021 3:28 PM
 */
@FunctionalInterface
public interface ChannelConnectAction {

    /**
     * Act.
     *
     * @param ctx           the ctx
     * @param remoteAddress the remote address
     * @param localAddress  the local address
     * @param promise       the promise
     */
    void act(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);

}
