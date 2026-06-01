package org.fz.nettyx.channel.bluetooth.server;

import io.netty.channel.*;
import io.netty.channel.oio.AbstractOioMessageChannel;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fz.nettyx.channel.bluetooth.BtDeviceAddress;

import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.net.SocketAddress;
import java.util.List;

@SuppressWarnings("deprecation")
public class BtServerChannel extends AbstractOioMessageChannel implements ServerChannel {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(BtServerChannel.class);

    private static final ChannelMetadata METADATA = new ChannelMetadata(false);

    private static final BtDeviceAddress LOCAL_ADDRESS = new BtDeviceAddress("localhost");

    private final StreamConnectionNotifier notifier;

    private final ChannelConfig config = new DefaultChannelConfig(this);

    private boolean open = true;

    public BtServerChannel(StreamConnectionNotifier notifier) {
        super(null);
        this.notifier = notifier;
    }

    @Override
    protected int doReadMessages(List<Object> buf) throws Exception {
        StreamConnection conn = notifier.acceptAndOpen();
        if (conn != null) {
            try {
                buf.add(new BtAcceptedChannel(this, conn));
                return 1;
            } catch (Exception e) {
                conn.close();
                throw e;
            }
        }
        return 0;
    }

    @Override
    protected void doBind(SocketAddress localAddress) {
    }

    @Override
    protected void doClose() throws Exception {
        open = false;
        try {
            notifier.close();
        } catch (Exception e) {
            log.warn("Failed to close notifier", e);
        }
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof OioEventLoopGroup;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public boolean isActive() {
        return isOpen();
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
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
    public ChannelConfig config() {
        return config;
    }

}
