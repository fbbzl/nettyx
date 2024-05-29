package org.fz.nettyx.channel;

import io.netty.channel.ChannelPromise;
import io.netty.channel.oio.AbstractOioChannel;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

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

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new SerialCommPortUnsafe();
    }

    @Override
    public SerialCommAddress localAddress() {
        return (SerialCommAddress) super.localAddress();
    }

    @Override
    public SerialCommAddress remoteAddress() {
        return (SerialCommAddress) super.remoteAddress();
    }

    @Override
    protected SerialCommAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected SerialCommAddress remoteAddress0() {
        return remoteAddress;
    }

    @Override
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException("doBind");
    }

    protected final class SerialCommPortUnsafe extends AbstractUnsafe {
        @Override
        public void connect(
                final SocketAddress remoteAddress,
                final SocketAddress localAddress, final ChannelPromise promise) {
            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }

            try {
                doConnect(remoteAddress, localAddress);
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

        private static final long   serialVersionUID = -870353013039000250L;
        private final        String value;

        /**
         * @return The serial port address of the device (e.g. COM1, /dev/ttyUSB0, ...)
         */
        public String value() {
            return value;
        }

    }
}

