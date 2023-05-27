package org.fz.nettyx.function;

import io.netty.channel.ChannelHandlerContext;

/**
 * The interface Channel exception action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /6/17 10:48
 */
@FunctionalInterface
public interface ChannelExceptionAction extends Action {

    /**
     * Act.
     *
     * @param t         the t
     * @param throwable the throwable
     */
    void act(ChannelHandlerContext t, Throwable throwable);

}
