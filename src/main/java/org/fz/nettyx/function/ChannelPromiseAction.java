package org.fz.nettyx.function;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 12/24/2021 3:31 PM
 */
@FunctionalInterface
public interface ChannelPromiseAction extends Action {

    void act(ChannelHandlerContext ctx, ChannelPromise promise);

}
