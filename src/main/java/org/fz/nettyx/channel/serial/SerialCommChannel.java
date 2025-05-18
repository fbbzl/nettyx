package org.fz.nettyx.channel.serial;

import io.netty.channel.ChannelPromise;
import io.netty.channel.oio.AbstractOioChannel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.fz.nettyx.channel.enhanced.EnhancedOioByteStreamChannel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.net.SocketAddress;


/**
 * cause of {@link AbstractOioChannel sync read task}
 * generic serial communication channel
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */

@SuppressWarnings("deprecation")
public abstract class SerialCommChannel extends EnhancedOioByteStreamChannel {

    protected static final SerialCommAddress LOCAL_ADDRESS = new SerialCommAddress("localhost");

    protected SerialCommAddress remoteAddress;

    protected boolean open = true;

    @Override
    public boolean isOpen()
    {
        return open;
    }

    @Override
    protected boolean isInputShutdown()
    {
        return !open;
    }

    @Override
    protected void doClose() throws Exception
    {
        try {
            super.doClose();
        } finally {
            open = false;
        }
    }

    protected abstract InputStream getInputStream() throws IOException;

    protected abstract OutputStream getOutputStream() throws IOException;

    @Override
    protected AbstractUnsafe newUnsafe()
    {
        return new SerialCommUnsafe();
    }

    @Override
    public SerialCommAddress localAddress()
    {
        return (SerialCommAddress) super.localAddress();
    }

    @Override
    public SerialCommAddress remoteAddress()
    {
        return (SerialCommAddress) super.remoteAddress();
    }

    @Override
    protected SerialCommAddress localAddress0()
    {
        return LOCAL_ADDRESS;
    }

    @Override
    protected SerialCommAddress remoteAddress0()
    {
        return remoteAddress;
    }

    @Override
    protected void doBind(SocketAddress localAddress)
    {
        throw new UnsupportedOperationException("doBind");
    }

    protected final class SerialCommUnsafe extends AbstractUnsafe {
        @Override
        public void connect(
                final SocketAddress  remoteAddress,
                final SocketAddress  localAddress,
                final ChannelPromise promise)
        {
            if (!promise.setUncancellable() || !ensureOpen(promise)) return;

            try {
                SerialCommChannel.this.doConnect(remoteAddress, localAddress);
                SerialCommChannel.super.activate(getInputStream(), getOutputStream());
                safeSetSuccess(promise);

                if (isActive()) pipeline().fireChannelActive();
            } catch (Exception t) {
                safeSetFailure(promise, t);
                closeIfClosed();
            }
        }
    }


    /**
     * @author fengbinbin
     * @version 1.0
     * @since 2024/5/15 12:44
     */
    @ToString
    @RequiredArgsConstructor
    public static class SerialCommAddress extends SocketAddress {

        @Serial private static final long   serialVersionUID = -870353013039000250L;
        private final                String value;

        /**
         * @return The serial port address of the device (e.g. COM1, /dev/ttyUSB0, ...)
         */
        public String value() {
            return value;
        }

    }
}

