package org.fz.nettyx.endpoint.client.rxtx.support;

import io.netty.channel.rxtx.RxtxChannel;
import io.netty.util.concurrent.DefaultEventExecutor;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/4 19:29
 */
public class NettyxRxtxChannel extends RxtxChannel {

    private final DefaultEventExecutor rxtxEventExecutors = new DefaultEventExecutor();

    @Override
    protected void doRead() {
        // do not use method reference!!!
        Runnable runnable = () -> NettyxRxtxChannel.super.doRead();
        rxtxEventExecutors.execute(runnable);
    }
}
