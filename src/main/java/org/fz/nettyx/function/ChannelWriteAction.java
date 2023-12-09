package org.fz.nettyx.function;

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

    /**
     * Action.
     *
     * @param ctx     the ctx
     * @param msg     the msg
     * @param promise the promise
     */
    void act(ChannelHandlerContext ctx, Object msg, ChannelPromise promise);
}
