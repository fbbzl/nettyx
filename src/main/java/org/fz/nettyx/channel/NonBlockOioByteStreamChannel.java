package org.fz.nettyx.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelConfig;
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
public abstract class NonBlockOioByteStreamChannel extends OioByteStreamChannel {

    protected NonBlockOioByteStreamChannel() {
        super(null);
    }

    @Override
    protected int doReadBytes(ByteBuf buf) {
        try {
            if (available() > 0) return super.doReadBytes(buf);
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    protected abstract void doInit();

    protected abstract int waitTime(ChannelConfig config);

}
