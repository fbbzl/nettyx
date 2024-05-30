package org.fz.nettyx.channel.bluetooth.client;

import com.intel.bluetooth.BlueCoveConfigProperties;
import com.intel.bluetooth.BlueCoveImpl;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import org.fz.nettyx.channel.EnhancedOioByteStreamChannel;
import org.fz.nettyx.channel.bluetooth.BtChannelConfig;
import org.fz.nettyx.channel.bluetooth.BtDeviceAddress;
import org.fz.nettyx.util.Try;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.util.function.Consumer;

/**
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */

public class BtChannel extends EnhancedOioByteStreamChannel {

    private static final BtDeviceAddress LOCAL_ADDRESS = new BtDeviceAddress("localhost");

    private BtDeviceAddress remoteAddress;

    private final BtChannelConfig config;

    private InputStream      inputStream;
    private OutputStream     outputStream;
    private StreamConnection streamConnection;

    public BtChannel() {
        config = new BtChannelConfig.DefaultBluetoothChannelConfig(this);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new BluetoothUnsafe();
    }

    @Override
    protected boolean isInputShutdown() {
        return false;
    }

    @Override
    protected ChannelFuture shutdownInput() {
        ChannelPromise promise = newPromise();

        EventLoop loop = eventLoop();

        Consumer<ChannelPromise> doShutdown = Try.accept(pro -> {
            try {
                inputStream.close();
                outputStream.close();
                pro.setSuccess();
            } catch (Exception t) {
                pro.setFailure(t);
            }
        });

        if (loop.inEventLoop()) {
            doShutdown.accept(promise);
        } else {
            loop.execute(() -> doShutdown.accept(promise));
        }

        return promise;
    }

    @Override
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException("doBind");
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        this.remoteAddress = (BtDeviceAddress) remoteAddress;

        config().getOptions().forEach((co, val) -> BlueCoveImpl.setConfigProperty(co.name(), String.valueOf(val)));

        if (config.getConnectTimeoutMillis() <= 0) {
            streamConnection = (StreamConnection) Connector.open(this.remoteAddress.value());
        } else {
            BlueCoveImpl.setConfigProperty(BlueCoveConfigProperties.PROPERTY_CONNECT_TIMEOUT, String.valueOf(config.getConnectTimeoutMillis()));
            streamConnection = (StreamConnection) Connector.open(this.remoteAddress.value(), Connector.READ_WRITE, true);
        }

        activate((inputStream = streamConnection.openInputStream()), (outputStream = streamConnection.openOutputStream()));
    }

    @Override
    protected SocketAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return remoteAddress;
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected void doClose() throws Exception {
        open = false;

        try {
            super.doClose();
        } finally {
            if (streamConnection != null) {
                streamConnection.close();
                streamConnection = null;
            }
        }
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    protected final class BluetoothUnsafe extends AbstractUnsafe {
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
                promise.setSuccess();

                if (!wasActive && isActive()) {
                    pipeline().fireChannelActive();
                }
            } catch (Exception t) {
                promise.setFailure(t);
                closeIfClosed();
            }
        }
    }

}
