package org.fz.nettyx.channel;

import io.netty.channel.Channel;
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

public abstract class SerialCommPortChannel extends OioByteStreamChannel {

    private final DefaultEventExecutor jscEventExecutors = new DefaultEventExecutor();

    /**
     * Create a new instance
     *
     * @param parent the parent {@link Channel} which was used to create this instance. This can be null if the
     *               {@link} has no parent as it was created by your self.
     */
    protected SerialCommPortChannel(Channel parent) {
        super(parent);
    }

    @Override
    public void doRead() {
        // do not use method reference!!!
        Runnable runnable = () -> SerialCommPortChannel.super.doRead();
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
