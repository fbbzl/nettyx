package org.fz.nettyx.channel.enhanced;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.oio.AbstractOioChannel;
import io.netty.channel.oio.OioByteStreamChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.EOFException;
import java.io.IOException;


/**
 * cause of {@link AbstractOioChannel sync blocking read task}
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */

@SuppressWarnings("deprecation")
public abstract class EnhancedOioByteStreamChannel extends OioByteStreamChannel {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(EnhancedOioByteStreamChannel.class);

    @Override
    protected ChannelFuture shutdownInput()
    {
        return newSucceededFuture();
    }

    protected EnhancedOioByteStreamChannel()
    {
        super(null);
    }

    protected EnhancedOioByteStreamChannel(io.netty.channel.Channel parent)
    {
        super(parent);
    }

    @Override
    protected int doReadBytes(ByteBuf buf)
    {
        try {
            // check before use, it will avoid blocking
            if (available() > 0) return super.doReadBytes(buf);
            return 0;
        } catch (EOFException e) {
            log.debug("doReadBytes reached EOF", e);
            unsafe().closeForcibly();
            return -1;
        } catch (IOException e) {
            log.debug("doReadBytes IO error", e);
            unsafe().closeForcibly();
            return -1;
        } catch (Exception e) {
            log.debug("doReadBytes failed", e);
            return 0;
        }
    }

    @Override
    protected void doDisconnect() throws Exception
    {
        doClose();
    }

}
