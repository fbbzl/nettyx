package org.fz.nettyx.channel.bluetooth;

import com.intel.bluetooth.BlueCoveConfigProperties;
import com.intel.bluetooth.BlueCoveImpl;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import org.fz.nettyx.channel.EnhancedOioByteStreamChannel;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.net.SocketAddress;

/**
 * TODO not completed
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */
public class BluetoothChannel extends EnhancedOioByteStreamChannel {

    private static final BluetoothDeviceAddress LOCAL_ADDRESS = new BluetoothDeviceAddress("localhost");

    private BluetoothDeviceAddress remoteAddress;

    private boolean opened = true;

    private final BluetoothChannelConfig config;

    private InputStream      inputStream;
    private StreamConnection streamConnection;

    public BluetoothChannel() {
        config = new BluetoothChannelOption.DefaultBluetoothChannelConfig(this);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new BluetoothUnsafe();
    }

    @Override
    protected boolean isInputShutdown() {
        return !opened;
    }

    @Override
    protected ChannelFuture shutdownInput() {
        ChannelPromise promise = newPromise();

        EventLoop loop = eventLoop();
        if (loop.inEventLoop()) {
            shutdownInput0(promise);
        } else {
            loop.execute(() -> shutdownInput0(promise));
        }

        return promise;
    }

    private void shutdownInput0(final ChannelPromise promise) {
        try {
            inputStream.close();
            promise.setSuccess();
        } catch (Exception t) {
            promise.setFailure(t);
        }
    }

    @Override
    protected void doBind(SocketAddress localAddress) {
        throw new UnsupportedOperationException("doBind");
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        this.remoteAddress = (BluetoothDeviceAddress) remoteAddress;

        if (config.getConnectTimeoutMillis() <= 0) {
            streamConnection = (StreamConnection) Connector.open(this.remoteAddress.value());
        } else {
            BlueCoveImpl.setConfigProperty(BlueCoveConfigProperties.PROPERTY_CONNECT_TIMEOUT, String.valueOf(config.getConnectTimeoutMillis()));
            streamConnection = (StreamConnection) Connector.open(this.remoteAddress.value(), Connector.READ_WRITE, true);
        }

        inputStream = streamConnection.openInputStream();

        activate(inputStream, streamConnection.openOutputStream());
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
        opened = false;

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

        return opened;
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
            } catch (Throwable t) {
                promise.setFailure(t);
                closeIfClosed();
            }
        }
    }

    public static class BluetoothDeviceAddress extends SocketAddress {

        private final String value;

        public BluetoothDeviceAddress(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
