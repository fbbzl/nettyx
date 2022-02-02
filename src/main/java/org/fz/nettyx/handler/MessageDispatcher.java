package org.fz.nettyx.handler;

import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author fengbinbin
 * @since 2022-02-02 14:32
 **/
public abstract class MessageDispatcher<I> extends SimpleChannelInboundHandler<I> {

}
