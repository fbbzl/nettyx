package org.fz.nettyx.endpoint.client.rxtx.support;

import io.netty.channel.rxtx.RxtxChannel;
import io.netty.util.concurrent.DefaultEventExecutor;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/4 19:29
 */

@SuppressWarnings("deprecation")
public class NettyxRxtxChannel extends RxtxChannel {

    private final DefaultEventExecutor rxtxEventExecutors = new DefaultEventExecutor();

    @Override
    public void doRead() {
        // do not use method reference!!!
        Runnable runnable = () -> NettyxRxtxChannel.super.doRead();
        rxtxEventExecutors.execute(runnable);
    }
}
