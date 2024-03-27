package org.fz.nettyx.channel;

import com.fazecast.jSerialComm.SerialPortTimeoutException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.oio.AbstractOioChannel;
import io.netty.channel.oio.OioByteStreamChannel;
import io.netty.util.concurrent.DefaultEventExecutor;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelOption.WAIT_TIME;

/**
 * cause of {@link AbstractOioChannel sync read task}
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */

@SuppressWarnings("deprecation")
public abstract class SerialCommPortChannel extends OioByteStreamChannel {

    protected static final SerialCommPortAddress LOCAL_ADDRESS = new SerialCommPortAddress("localhost");

    protected SerialCommPortAddress deviceAddress;

    private final DefaultEventExecutor jscEventExecutors = new DefaultEventExecutor();

    protected SerialCommPortChannel() {
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
    public SerialCommPortAddress localAddress() {
        return (SerialCommPortAddress) super.localAddress();
    }

    @Override
    public SerialCommPortAddress remoteAddress() {
        return (SerialCommPortAddress) super.remoteAddress();
    }

    @Override
    protected SerialCommPortAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected SerialCommPortAddress remoteAddress0() {
        return deviceAddress;
    }

    @Override
    protected boolean isInputShutdown() {
        return !open;
    }

    @Override
    protected int doReadBytes(ByteBuf buf) throws Exception {
        try {
            return super.doReadBytes(buf);
        }
        catch (SerialPortTimeoutException e) {
            return 0;
        }
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

    protected abstract void doInit() throws Exception;

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

                int waitTime = config().getOption(WAIT_TIME);
                if (waitTime > 0) {
                    eventLoop().schedule(() -> {
                        try {
                            doInit();
                            safeSetSuccess(promise);
                            if (!wasActive && isActive()) {
                                pipeline().fireChannelActive();
                            }
                        }
                        catch (Throwable t) {
                            safeSetFailure(promise, t);
                            closeIfClosed();
                        }
                    }, waitTime, TimeUnit.MILLISECONDS);
                }
                else {
                    doInit();
                    safeSetSuccess(promise);
                    if (!wasActive && isActive()) {
                        pipeline().fireChannelActive();
                    }
                }
            }
            catch (Throwable t) {
                safeSetFailure(promise, t);
                closeIfClosed();
            }
        }
    }
}
