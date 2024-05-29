package org.fz.nettyx.channel.bluetooth.client;

import com.intel.bluetooth.BlueCoveConfigProperties;
import com.intel.bluetooth.BlueCoveImpl;
import io.netty.channel.*;
import org.fz.nettyx.channel.EnhancedOioByteStreamChannel;
import org.fz.nettyx.channel.bluetooth.BluetoothChannelConfig;
import org.fz.nettyx.channel.bluetooth.BluetoothChannelOption;
import org.fz.nettyx.channel.bluetooth.BluetoothDeviceAddress;
import org.fz.nettyx.util.Try;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;
import java.util.Map;
import java.util.function.Consumer;

/**
 * TODO not completed
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/27 14:27
 */

public class BluetoothChannel extends EnhancedOioByteStreamChannel {

    private static final BluetoothDeviceAddress LOCAL_ADDRESS = new BluetoothDeviceAddress("localhost");

    private BluetoothDeviceAddress remoteAddress;

    private final BluetoothChannelConfig config;

    private InputStream      inputStream;
    private OutputStream     outputStream;
    private StreamConnection streamConnection;

    public BluetoothChannel() {
        config = new BluetoothChannelConfig.DefaultBluetoothChannelConfig(this);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new BluetoothUnsafe();
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

    protected void doInit() {
        Map<ChannelOption<?>, Object> options = config().getOptions();
        for (Map.Entry<ChannelOption<?>, Object> channelOptionEntry : options.entrySet()) {
            BluetoothChannelOption channelOption = (BluetoothChannelOption) channelOptionEntry.getKey();
            BlueCoveImpl.setConfigProperty(channelOption.name(), String.valueOf(channelOptionEntry.getValue()));
        }


        BlueCoveImpl.setConfigProperty(BlueCoveConfigProperties.PROPERTY_CONNECT_TIMEOUT, String.valueOf(config.getConnectTimeoutMillis()));

        super.activate(serialPort.getInputStream(), serialPort.getOutputStream());
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
