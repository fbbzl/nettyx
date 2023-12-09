package org.fz.nettyx.function;

import io.netty.channel.ChannelHandlerContext;

/**
 * The interface Channel message action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 12 /24/2021 3:19 PM
 */
@FunctionalInterface
public interface ChannelReadAction {

    /**
     * Act.
     *
     * @param t   the t
     * @param msg the msg
     */
    void act(ChannelHandlerContext t, Object msg);
}
