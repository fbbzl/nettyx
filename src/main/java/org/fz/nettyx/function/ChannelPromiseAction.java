package org.fz.nettyx.function;

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
public interface ChannelPromiseAction extends Action {

    /**
     * Act.
     *
     * @param ctx     the ctx
     * @param promise the promise
     */
    void act(ChannelHandlerContext ctx, ChannelPromise promise);

}
