package org.fz.nettyx.channel;

import io.netty.channel.oio.AbstractOioChannel;
import io.netty.channel.oio.OioByteStreamChannel;
import io.netty.util.concurrent.DefaultEventExecutor;

/**
 * cause of {@link AbstractOioChannel sync read task}
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */

public abstract class NettyxOioByteStreamChannel extends OioByteStreamChannel {

    private final DefaultEventExecutor jscEventExecutors = new DefaultEventExecutor();

    @Override
    public void doRead() {
        // do not use method reference!!!
        Runnable runnable = () -> NettyxOioByteStreamChannel.super.doRead();
        jscEventExecutors.execute(runnable);
    }

    @Override
    protected void doClose() throws Exception {
        try {
            super.doClose();
        }
        finally {
            this.jscEventExecutors.shutdownGracefully();
        }
    }
}
