package org.fz.nettyx.channel.bluetooth.client;

import com.intel.bluetooth.BlueCoveConfigProperties;
import com.intel.bluetooth.BlueCoveImpl;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import org.fz.nettyx.channel.EnhancedOioByteStreamChannel;
import org.fz.nettyx.channel.bluetooth.BtChannelConfig;
import org.fz.nettyx.channel.bluetooth.BtDeviceAddress;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.net.SocketAddress;

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

    private boolean open;

    private InputStream      inputStream;
    private StreamConnection streamConnection;

    public BtChannel() {
        config = new BtChannelConfig.DefaultBluetoothChannelConfig(this);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new BluetoothUnsafe();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    protected boolean isInputShutdown() {
        return !isOpen();
    }

    @Override
    protected ChannelFuture shutdownInput() {
        ChannelPromise pro = newPromise();

        try {
            inputStream.close();
            pro.setSuccess();
        } catch (Exception t) {
            pro.setFailure(t);
        }

        return pro;
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
        open = true;
        activate((inputStream = streamConnection.openInputStream()), streamConnection.openOutputStream());
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
