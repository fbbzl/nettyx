package org.fz.nettyx.channel.bluetooth.server;

import io.netty.channel.*;
import org.fz.nettyx.channel.bluetooth.BtDeviceAddress;
import org.fz.nettyx.channel.enhanced.EnhancedOioByteStreamChannel;

import javax.microedition.io.StreamConnection;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketAddress;

public class BtAcceptedChannel extends EnhancedOioByteStreamChannel {

    private static final BtDeviceAddress LOCAL_ADDRESS = new BtDeviceAddress("localhost");

    private final ChannelConfig config = new DefaultChannelConfig(this);

    private final StreamConnection connection;

    private boolean open = true;

    private InputStream inputStream;

    public BtAcceptedChannel(Channel parent, StreamConnection connection) throws Exception {
        super(parent);
        this.connection = connection;
        this.inputStream = connection.openInputStream();
        OutputStream outputStream = null;
        try {
            outputStream = connection.openOutputStream();
            activate(inputStream, outputStream);
        } catch (Exception e) {
            closeQuietly(inputStream);
            closeQuietly(outputStream);
            throw e;
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
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
            if (inputStream != null) inputStream.close();
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
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) {
        throw new UnsupportedOperationException("doConnect");
    }

    @Override
    protected SocketAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

    @Override
    protected void doClose() throws Exception {
        open = false;
        try {
            super.doClose();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Override
    public ChannelConfig config() {
        return config;
    }

}
