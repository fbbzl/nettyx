package nettyx.function;

import io.netty.channel.ChannelHandlerContext;

/**
 * The interface Channel event action.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /6/17 10:50
 */
@FunctionalInterface
public interface ChannelEventAction {

    /**
     * Act.
     *
     * @param ctx the ctx
     * @param evt the evt
     */
    void act(ChannelHandlerContext ctx, Object evt);

}
