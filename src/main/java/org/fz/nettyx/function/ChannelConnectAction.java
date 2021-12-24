package org.fz.nettyx.function;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 12/24/2021 3:28 PM
 */
@FunctionalInterface
public interface ChannelConnectAction {

    void act(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise);

}
