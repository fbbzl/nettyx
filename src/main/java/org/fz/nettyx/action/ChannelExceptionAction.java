package org.fz.nettyx.action;

import io.netty.channel.ChannelHandlerContext;

/**
 * The interface Channel exception action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /6/17 10:48
 */
@FunctionalInterface
public interface ChannelExceptionAction {

    ChannelExceptionAction DO_NOTHING = (ctx, thro) -> {
    };

    void act(ChannelHandlerContext ctx, Throwable throwable);

}
