package org.fz.nettyx.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.oio.AbstractOioChannel;
import io.netty.channel.oio.OioByteStreamChannel;
import io.netty.util.concurrent.DefaultEventExecutor;
import lombok.RequiredArgsConstructor;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;


/**
 * cause of {@link AbstractOioChannel sync blocking read task}
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */

@SuppressWarnings("deprecation")
public abstract class ReadAsyncOioByteStreamChannel extends OioByteStreamChannel {

    protected static final SerialCommAddress LOCAL_ADDRESS = new SerialCommAddress("localhost");

    protected SerialCommAddress deviceAddress;

    private final DefaultEventExecutor eventExecutors = new DefaultEventExecutor();

    protected ReadAsyncOioByteStreamChannel() {
        super(null);
    }

    protected boolean open = true;

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new SerialCommPortUnsafe();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public SocketAddress localAddress() {
        return super.localAddress();
    }

    @Override
    public SocketAddress remoteAddress() {
        return super.remoteAddress();
    }

    @Override
    protected SocketAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return deviceAddress;
    }

    @Override
    protected boolean isInputShutdown() {
        return !open;
    }

    @Override
    protected int doReadBytes(ByteBuf buf) {
        try {
            return super.doReadBytes(buf);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void doRead() {
        // do not use method reference!!!
        Runnable runnable = () -> ReadAsyncOioByteStreamChannel.super.doRead();
        eventExecutors.execute(runnable);
    }

    @Override
    protected void doClose() throws Exception {
        try {
            super.doClose();
        } finally {
            this.eventExecutors.shutdownGracefully();
        }
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected ChannelFuture shutdownInput() {
        return newFailedFuture(new UnsupportedOperationException("shutdownInput"));
    }

    @Override
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException();
    }

    protected abstract void doInit();

    protected abstract int waitTime(ChannelConfig config);

    protected final class SerialCommPortUnsafe extends AbstractUnsafe {
        @Override
        public void connect(
                final SocketAddress remoteAddress,
                final SocketAddress localAddress, final ChannelPromise promise) {
            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }

            try {
                final boolean wasActive = isActive();
                doConnect(remoteAddress, localAddress);

                int waitTime = waitTime(config());
                if (waitTime > 0) {
                    eventLoop().schedule(() -> {
                        try {
                            doInit();
                            safeSetSuccess(promise);
                            if (!wasActive && isActive()) {
                                pipeline().fireChannelActive();
                            }
                        } catch (Exception t) {
                            safeSetFailure(promise, t);
                            closeIfClosed();
                        }
                    }, waitTime, TimeUnit.MILLISECONDS);
                } else {
                    doInit();
                    safeSetSuccess(promise);
                    if (!wasActive && isActive()) {
                        pipeline().fireChannelActive();
                    }
                }
            } catch (Exception t) {
                safeSetFailure(promise, t);
                closeIfClosed();
            }
        }
    }

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

        @Override
        public String toString() {
            return "SerialCommPortAddress{" +
                   "value='" + value + '\'' +
                   '}';
        }
    }
}
