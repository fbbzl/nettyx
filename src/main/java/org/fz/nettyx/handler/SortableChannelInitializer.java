package org.fz.nettyx.handler;

import io.netty.channel.Channel;

/**
 * @author fengbinbin
 * @since 2022-02-02 16:17
 **/
public abstract class SortableChannelInitializer<C extends Channel> extends io.netty.channel.ChannelInitializer<C> {

    protected abstract void order();

}
