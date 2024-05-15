package org.fz.nettyx.channel;

import com.intel.bluetooth.BlueCoveConfigProperties;
import com.intel.bluetooth.BlueCoveImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.oio.OioByteStreamChannel;
import org.fz.nettyx.endpoint.bluetooth.BluetoothChannelConfig;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.InputStream;
import java.net.SocketAddress;

@SuppressWarnings("deprecation")
public class OioBluetoothChannel extends OioByteStreamChannel {

    private static final BluetoothDeviceAddress LOCAL_ADDRESS = new BluetoothDeviceAddress("localhost");

    private BluetoothDeviceAddress remoteDeviceAddress;

    private boolean opened = true;

    private final BluetoothChannelConfig config;

    private InputStream inputStream;
    private StreamConnection streamConnection;


    public OioBluetoothChannel() {

        super(null);
        config = new DefaultBluetoothChannelConfig(this);
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
        } catch (Throwable t) {

            promise.setFailure(t);
        }
    }

    /*
     * Fixed the blocking read in EventLoop(ThreadPerChannelEventLoop)
     * */
    @Override
    protected int doReadBytes(ByteBuf buf) throws Exception {

        if (available() > 0) {

            return super.doReadBytes(buf);
        }

        return 0;
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {

        remoteDeviceAddress = (BluetoothDeviceAddress) remoteAddress;

        if (config.getConnectTimeoutMillis() <= 0) {

            streamConnection = (StreamConnection) Connector.open(remoteDeviceAddress.value());
        } else {

            BlueCoveImpl.setConfigProperty(BlueCoveConfigProperties.PROPERTY_CONNECT_TIMEOUT, String.valueOf(config.getConnectTimeoutMillis()));
            streamConnection = (StreamConnection) Connector.open(remoteDeviceAddress.value(), Connector.READ_WRITE, true);
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

        return remoteDeviceAddress;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {

        throw new UnsupportedOperationException("doBind");
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

    @Override
    protected AbstractUnsafe newUnsafe() {

        return new AbstractUnsafe() {
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
        };
    }
}
