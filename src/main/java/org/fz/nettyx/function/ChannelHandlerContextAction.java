package org.fz.nettyx.function;

import io.netty.channel.ChannelHandlerContext;

/**
 * The interface Channel handler context action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/26 11:09
 */
@FunctionalInterface
public interface ChannelHandlerContextAction extends Action {

    /**
     * Act.
     *
     * @param ctx the ctx
     */
    void act(ChannelHandlerContext ctx);
}
