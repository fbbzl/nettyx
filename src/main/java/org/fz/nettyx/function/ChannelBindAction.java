package org.fz.nettyx.function;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 12/24/2021 3:26 PM
 */
@FunctionalInterface
public interface ChannelBindAction extends Action {

    void act(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise);
}
