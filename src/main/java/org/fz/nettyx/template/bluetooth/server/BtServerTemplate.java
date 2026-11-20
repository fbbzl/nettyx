package org.fz.nettyx.template.bluetooth.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.AccessLevel;
import lombok.Getter;
import org.fz.nettyx.channel.bluetooth.server.BtServerChannel;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnectionNotifier;
import java.net.InetSocketAddress;

/**
 * Sets up a Bluetooth server bootstrap and initializes channels for accepted connections.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-06-02
 */
@Getter
@SuppressWarnings("deprecation")
public abstract class BtServerTemplate {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(BtServerTemplate.class);
    private final        EventLoopGroup parentEventLoopGroup, childEventLoopGroup;

    private final ServerBootstrap serverBootstrap;
    @Getter(AccessLevel.NONE)
    private StreamConnectionNotifier unboundNotifier;

    protected BtServerTemplate(String serverUUID) {
        this(serverUUID, null, false, false, false);
    }

    protected BtServerTemplate(String serverUUID, String serverName) {
        this(serverUUID, serverName, false, false, false);
    }

    protected BtServerTemplate(String serverUUID, String serverName, boolean authenticate, boolean encrypt, boolean master) {
        this(openNotifier(serverUUID, serverName, authenticate, encrypt, master));
    }

    BtServerTemplate(StreamConnectionNotifier notifier) {
        this.unboundNotifier = notifier;
        EventLoopGroup child = null, parent = null;
        try {
            this.childEventLoopGroup = child = childEventLoopGroup();
            this.parentEventLoopGroup = parent = parentEventLoopGroup();
            this.serverBootstrap = newServerBootstrap(notifier);
        } catch (RuntimeException | Error e) {
            closeUnboundNotifier();
            if (child != null) child.shutdownGracefully();
            if (parent != null) parent.shutdownGracefully();
            throw e;
        }
    }

    private static StreamConnectionNotifier openNotifier(
            String serverUUID, String serverName, boolean authenticate, boolean encrypt, boolean master) {
        try {
            LocalDevice local = LocalDevice.getLocalDevice();
            if (!local.setDiscoverable(DiscoveryAgent.GIAC)) {
                log.warn("Failed to set bluetooth device discoverable");
            }

            StringBuilder url = new StringBuilder("btspp://localhost:").append(serverUUID);
            if (serverName != null) url.append(";name=").append(serverName);
            if (authenticate) url.append(";authenticate=true");
            if (encrypt) url.append(";encrypt=true");
            if (master) url.append(";master=true");

            return (StreamConnectionNotifier) Connector.open(url.toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Bluetooth server", e);
        }
    }

    protected EventLoopGroup parentEventLoopGroup() {
        return new OioEventLoopGroup();
    }

    protected EventLoopGroup childEventLoopGroup() {
        return new OioEventLoopGroup();
    }

    public ChannelFuture bind() {
        ChannelFuture bindFuture = this.getServerBootstrap().clone().bind();
        log.debug("already finish bind, bind future is [{}]", bindFuture);
        return bindFuture;
    }

    protected ServerBootstrap newServerBootstrap(StreamConnectionNotifier notifier) {
        return new ServerBootstrap()
                .group(parentEventLoopGroup, childEventLoopGroup)
                .localAddress(new InetSocketAddress(0))
                .channelFactory(() -> newServerChannel(notifier))
                .childHandler(childChannelInitializer());
    }

    private synchronized BtServerChannel newServerChannel(StreamConnectionNotifier notifier) {
        if (unboundNotifier != notifier) throw new IllegalStateException("Bluetooth notifier is no longer available");
        BtServerChannel channel = new BtServerChannel(notifier);
        unboundNotifier = null;
        return channel;
    }

    private synchronized void closeUnboundNotifier() {
        StreamConnectionNotifier notifier = unboundNotifier;
        unboundNotifier = null;
        if (notifier != null) {
            try {
                notifier.close();
            } catch (Exception e) {
                log.warn("Failed to close unbound notifier", e);
            }
        }
    }

    protected abstract ChannelInitializer<? extends Channel> childChannelInitializer();

    protected void shutdownGracefully() {
        closeUnboundNotifier();
        childEventLoopGroup.shutdownGracefully();
        parentEventLoopGroup.shutdownGracefully();
    }

    protected void syncShutdownGracefully() throws InterruptedException {
        closeUnboundNotifier();
        childEventLoopGroup.shutdownGracefully().sync();
        parentEventLoopGroup.shutdownGracefully().sync();
        log.debug("has already successfully shutdown");
    }

}
