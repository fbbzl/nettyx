package org.fz.nettyx.channel.enhanced;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.oio.AbstractOioChannel;
import io.netty.channel.oio.OioByteStreamChannel;


/**
 * cause of {@link AbstractOioChannel sync blocking read task}
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */

@SuppressWarnings("deprecation")
public abstract class EnhancedOioByteStreamChannel extends OioByteStreamChannel {

    @Override
    protected ChannelFuture shutdownInput() {
        return newSucceededFuture();
    }

    protected EnhancedOioByteStreamChannel() {
        super(null);
    }

    @Override
    protected int doReadBytes(ByteBuf buf)
    {
        try {
            // check before use, it will avoid blocking
            if (available() > 0) return super.doReadBytes(buf);
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void doDisconnect() throws Exception
    {
        doClose();
    }

}
